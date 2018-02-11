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

function main() {
  var client = new cat_proto.Cat('localhost:6431',
      grpc.credentials.createInsecure());
  var uid;
  if (process.argv.length >= 3) {
    uid = int.Parse(process.argv[2]);
  } else {
    uid = '2001000';
  }
  client.createNeImAccount({uid: uid, name: '在水一方', token: 'sdfsfsdf'}, function(err, response) {
    console.log('response:', response);
  });
}

main();
