const db = require('./database')

module.exports.getUserIds = async function () {
  let users = await db.query(`SELECT ID FROM cwd_user;`)
  return users.map(p => p.ID)
}

module.exports.getUserKeys = async function () {
  let users = await db.query(`SELECT user_name FROM cwd_user;`)
  return users.map(p => p.user_name)
}
