package hello 

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import kotlin.test.assertEquals
import org.junit.Test

class TestSource {
  @Test fun f() {
    val df = async(CommonPool) {
      workload(n = 1)
    }

    runBlocking {
      df.await()
    }
  }
}
