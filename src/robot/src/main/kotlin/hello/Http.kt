package hello

import com.google.protobuf.Message
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.RequestBody
import okhttp3.MediaType

import protofiles.protojava.CommonProtos as CommonProto
import protofiles.protojava.CodeInProtos as CodeInProto

class Http {
    interface GwCallback {
        fun onError()
        fun onReply(reply: com.google.protobuf.Message)
    }

    private val client = OkHttpClient()

    var name = "My Ng ClientTest"
    var gwUrl = "https://gw.codein.net/api/"

    @Throws(Exception::class)
    fun get(url: String = "http://publicobject.com/helloworld.txt") {
        val request = Request.Builder()
                .url(url)
                //.tag(this)
                .build()

        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                //val http = call.request().tag() as Http
                //println("tag: ${http.name}")
                response.body()!!.use { responseBody ->
                    if (!response.isSuccessful) throw IOException("Unexpected code " + response)

                    val responseHeaders = response.headers()
                    var i = 0
                    val size = responseHeaders.size()
                    while (i < size) {
                        println(responseHeaders.name(i) + ": " + responseHeaders.value(i))
                        i++
                    }

                    println(responseBody.string())
                }
            }
        })
    }

    fun post(url: String, json: String) {
        val JSON = MediaType.parse("application/json; charset=utf-8")
        val body = RequestBody.create(JSON, json)
        val request = Request.Builder()
                .url(url)
                .post(body)
                .tag(this)
                .build()

        val call = client.newCall(request)
        call.enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val http = call.request().tag() as Http
                println("tag: ${http.name}")
                response.body()!!.use { responseBody ->
                    if (!response.isSuccessful) throw IOException("Unexpected code " + response)

                    val responseHeaders = response.headers()
                    var i = 0
                    val size = responseHeaders.size()
                    while (i < size) {
                        println(responseHeaders.name(i) + ": " + responseHeaders.value(i))
                        i++
                    }

                    println(responseBody.string())
                }
            }
        })

    }

    fun gwCall(api: String, req: com.google.protobuf.Message, builder: com.google.protobuf.Message.Builder, callback: GwCallback?) {
        val mtype = MediaType.parse("application/octet-stream")
        val body = RequestBody.create(mtype, req.toByteArray())
        val request = Request.Builder()
                .url(gwUrl + api)
                .post(body)
                .build()

        val call = client.newCall(request)
        call.enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback?.onError()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                response.body()!!.use { responseBody ->
                    if (!response.isSuccessful) throw IOException("Unexpected code " + response)

                    var reply = builder.mergeFrom(responseBody.byteStream()).build()
                    callback?.onReply(reply)
                }
            }
        })
    }

    fun post(req: com.google.protobuf.Message, rep: com.google.protobuf.Message.Builder) {
        val reqStr = req.toString()
        println("request: $reqStr")
        var b = CodeInProto.LoginReply.newBuilder()
        b.liveTopic = "topic/live"
        b.userInfoBuilder.uid = 2017002L
        b.userInfoBuilder.name = "青云"
        var msg = b.build()

        var ba = msg.toByteArray()
        rep.mergeFrom(ba)
    }

    companion object {

        private fun testGw(testId: Int) {
            val req = CodeInProto.GetMwRequest.newBuilder()
            req.gameId = 0
            val clinfo = req.clientInfoBuilder
            clinfo.uid = 2017009L
            clinfo.token = "kt_coro_http"
            clinfo.deviceId = "kt_coro_dev_000"
            val reply = CodeInProto.GetMwReply.newBuilder()
            val start = System.currentTimeMillis()
            Http().gwCall("GetMw", req.build(), reply, object: GwCallback {
                override fun onReply(reply: Message) {
                    if (reply is CodeInProto.GetMwReply) {
                        val timeCost = System.currentTimeMillis() - start
                        (0 until reply.listCount)
                                .map { reply.getList(it) }
                                .forEach { println("id: ${it.id} ${it.words}") }
                        println("test-$testId time cost: ${timeCost}ms latency: ${reply.latency}us, time: ${reply.timeUs}us")
                    }

                }

                override fun onError() {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

            })
        }

        private fun testProtoMsg(msg: CodeInProto.LoginReply) {
            val msgStr = msg.toString()
            println("reply: $msgStr")
            var b = msg.toBuilder()
            b.userInfoBuilder.name = "John Great"
            var newMsg = b.build()
            val str1 = msg.toString()
            val str2 = newMsg.toString()
            println("orig msg: $str1, new msg: $str2")
        }

        private fun testPb() {
             var http = Http()
            //Http().get("http://gw.codein.net/protocol.html")
            http.post("https://gw.codein.net/echo", "{\"hello world\"}")
            //http.post("test", 0, Reply())
            //http.get("http://gw.codein.net/protocol.html")
            //http.post("https://gw.codein.net/echo", "{\"hello world\"}")
            var builder = CodeInProto.LoginRequest.newBuilder()
            builder.phone = "18011112222"
            builder.passwd = "EatChickenTonight"
            var req = builder.build()
            var repBuilder = CodeInProto.LoginReply.newBuilder()
            http.post(req, repBuilder)
            var msg = repBuilder.build()

            testProtoMsg(msg)
        }

        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            (0 until 100).forEach {
                testGw(it)
            }
        }
    }
}
