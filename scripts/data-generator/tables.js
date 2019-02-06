const db = require('./database')
const fs = require('fs')
const path = require('path')

module.exports.createIfNotExist = async function () {
  let filePath = path.join(__dirname, 'AO_6797AA.sql')
  let q = fs.readFileSync(filePath, { encoding: 'utf-8' })
  for (let line of q.split('\n')) {
    if (!line.trim()) continue
    await db.query(line)
  }
}
