/*
 *
 * Copyright 2015 gRPC authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

var PROTO_PATH = __dirname + '/cat.proto';

var grpc = require('grpc');
var cat_proto = grpc.load(PROTO_PATH).cat_proto;

var https = require("https");
var qstring=require("querystring");

var appSecrete = "42e0888017c0";
var appKey = "481c15cf4d666fa3f3f6110e95c7917b";

var pg=require("./pg");
var crypto=require('crypto');
/**
 * Implements the SayHello RPC method.
 */
function createNeImAccount(call, callback) {
  pg.getNeAccount(call.request.uid).then(function(res) {
    if (res) {
      var reply = {
        msg: 'ok',
        account: res.account,
        token: res.token
      }
      callback(null, reply);
      return;
    }
    createNeImAccid(call, callback);
  })
}

function createNeImAccid(call, callback) {
  console.log(call.request)
  var timestamp=Math.round(new Date().getTime()/1000);
  var nonce=Math.floor(Math.random()*1e16).toString(16);
  var checksum = new Buffer(crypto.createHash('sha1').update(appSecrete + nonce + timestamp).digest()).toString('hex')
  var options = {
          hostname: 'api.netease.im',
          //port: 443,
          path: '/nimserver/user/create.action',
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8',
            'AppKey': appKey,
            'Nonce': nonce,
            'CurTime': timestamp,
            'CheckSum': checksum
          }
  };

  var req = https.request(options, function(res) {
      console.log('Status: ' + res.statusCode);
      console.log('Headers: ' + JSON.stringify(res.headers));
      res.setEncoding('utf8');
      res.on('data', function (body) {
          console.log('Body: ' + body);
          var js = JSON.parse(body);
          var reply;
          if (js.code == 200) {
            pg.saveNeAccount(call.request.uid, js.info.accid, js.info.token);
            reply = {
              msg: 'ok',
              account: js.info.accid,
              token: js.info.token
            }
          } else {
            reply = {
              err: js.code,
              msg: js.desc
            }
            if (reply.err == 0)
              reply.err = -1;
          }
          callback(null, reply);
          });
      });

  req.on('error', function(e) {
      console.log('problem with request: ' + e.message);
      });
  // write data to request body
  var accid='ne' + (call.request.uid * 11).toString();
  req.write(qstring.stringify({"accid": accid, "name": call.request.name}));
  req.end();
}

/**
 * Starts an RPC server that receives requests for the Greeter service at the
 * sample server port
 */
function main() {
  pg.connect().then(function(r) {
  })
  console.log("cat grpc starting ...")
  var server = new grpc.Server();
  server.addService(cat_proto.Cat.service, {createNeImAccount: createNeImAccount});
  server.bind('0.0.0.0:6431', grpc.ServerCredentials.createInsecure());
  server.start();
}

main();
