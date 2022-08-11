package pw.binom.docker.console

object DetachConsole : Console {
    override suspend fun asyncClose() {
        // Do nothing
    }
}
