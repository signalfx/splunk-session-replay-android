package utils

class Version(private val code: Int) : Comparable<Version> {

    override fun compareTo(other: Version): Int {
        return code.compareTo(other.code)
    }

    companion object {

        operator fun invoke(major: Int, minor: Int, patch: Int = 0): Version {
            val m = (major and 0xFF) shl 24
            val n = (minor and 0xFF) shl 16
            val p = (patch and 0xFFFF)
            return Version(m or n or p)
        }

        operator fun invoke(version: String): Version {
            val cleanVersion = version.substringBefore('-').substringBefore('+')
            val parts = cleanVersion.split('.')

            val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0

            return Version(major, minor, patch)
        }
    }
}
