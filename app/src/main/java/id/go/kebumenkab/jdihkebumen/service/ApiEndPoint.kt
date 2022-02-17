package id.go.kebumenkab.jdihkebumen.service

class ApiEndPoint {
    companion object {

        private val SERVER = "https://jdih.kebumenkab.go.id/index.php/android/"
        val CREATE = SERVER+"insert_device"
        val READ = SERVER+"read.php"
        val DELETE = SERVER+"delete.php"
        val UPDATE = SERVER+"update.php"

    }
}