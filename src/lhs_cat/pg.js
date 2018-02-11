
var pgClient;
const {Pool,Client} = require('pg').native

exports.connect = async function () {
	pgClient = new Client({
		user: 'dba',
		host: 'localhost',
		database: 'codein',
		password: 'real-password-here',
		port: 5432,
	})

	await pgClient.connect()
}

exports.close = async function () {
	await pgClient.end()
}

exports.saveNeAccount = function (uid, account, token) {
	pgClient.query('insert into ne_accounts(uid,account,token) values($1,$2,$3) on conflict(uid) do update set account=excluded.account,token=excluded.token,timestamp=now()', [uid,account,token])
}

exports.getNeAccount = async function (uid) {
	const res = await pgClient.query('select account,token,extract(epoch from timestamp) as time_ms from ne_accounts where uid=$1', [uid])
  if (res.rows[0]) {
    return {account: res.rows[0].account, token: res.rows[0].token, time_ms: res.rows[0].time_ms}
  }
}

