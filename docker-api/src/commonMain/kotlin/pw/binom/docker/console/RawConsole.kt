package pw.binom.docker.console

import pw.binom.io.AsyncChannel

class RawConsole(val channel: AsyncChannel) : Console, AsyncChannel by channel