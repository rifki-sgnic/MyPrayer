package com.rfksgn.myprayer.core.sensor

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Heading in degrees from true north, whether the magnetometer is calibrated well enough to trust it, and
 * whether the phone lies flat enough for the heading to hold (it degenerates as the phone stands upright).
 */
data class CompassReading(val azimuth: Float, val reliable: Boolean, val flat: Boolean = true)

// Tilt from lying flat, as the cosine: past 45° ask the user to lay the phone down, and stop asking only
// once it is back within 35°, so a hand hovering near the edge doesn't toggle the hint
private const val TiltedBeyond = 0.707f
private const val FlatWithin = 0.819f

class CompassSensorManager(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // The fused vector needs a gyroscope. Budget phones without one often have only the geomagnetic vector,
    // or neither, in which case the raw accelerometer and magnetometer still give a heading.
    private val rotation: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)

    /** False when the phone cannot find north at all; Kiblat then shows the bearing without a compass. */
    val isAvailable: Boolean = magnetometer != null && (rotation != null || accelerometer != null)

    fun readings(latitude: Double, longitude: Double): Flow<CompassReading> = callbackFlow {
        // Magnetic north to true north; the declination doesn't change within a session
        val declination = GeomagneticField(latitude.toFloat(), longitude.toFloat(), 0f, System.currentTimeMillis())
            .declination
        val matrix = FloatArray(9)
        val angles = FloatArray(3)
        val gravity = FloatArray(3)
        val field = FloatArray(3)
        var hasGravity = false
        var hasField = false
        // The magnetometer's accuracy is the calibration signal on every path, fused or raw. Nothing is sent
        // until it has reported once, so the screen never flashes a calibration notice while it opens.
        var reliable: Boolean? = null
        var flat = true

        fun send() {
            val trusted = reliable ?: return
            SensorManager.getOrientation(matrix, angles)
            val azimuth = (Math.toDegrees(angles[0].toDouble()).toFloat() + declination + 720f) % 360f
            // matrix[8]: the screen's normal against world up, 1 lying flat and 0 standing upright
            flat = matrix[8] >= if (flat) TiltedBeyond else FlatWithin
            trySend(CompassReading(azimuth, trusted, flat))
        }

        // Light low-pass for the raw path; the fused vectors are already filtered
        fun smooth(input: FloatArray, into: FloatArray, primed: Boolean) {
            for (i in 0..2) into[i] = if (primed) into[i] + 0.2f * (input[i] - into[i]) else input[i]
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        reliable = event.accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
                        if (rotation != null) return
                        smooth(event.values, field, hasField)
                        hasField = true
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        smooth(event.values, gravity, hasGravity)
                        hasGravity = true
                    }
                    else -> {
                        SensorManager.getRotationMatrixFromVector(matrix, event.values)
                        send()
                        return
                    }
                }
                if (hasGravity && hasField && SensorManager.getRotationMatrix(matrix, null, gravity, field)) send()
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    reliable = accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
                }
            }
        }

        if (isAvailable) {
            sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, rotation ?: accelerometer, SensorManager.SENSOR_DELAY_UI)
        }

        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
