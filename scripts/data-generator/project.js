const db = require('./database')

module.exports.getProjectIds = async function () {
  let projects = await db.query(`SELECT ID FROM project;`)
  return projects.map(p => parseInt(p.id))
}
