const pgp = require('pg-promise')()

const client = pgp({
  user: process.env.POSTGRESQL_USERNAME || 'postgres',
  host: process.env.POSTGRESQL_DBHOST || 'jira-data-center-jiradcstack-1fvu0vx3mq6ph.c27ef3f22zvq.us-east-2.rds.amazonaws.com',
  database: process.env.POSTGRESQL_DATABASE || 'jira',
  password: process.env.POSTGRESQL_PASSWORD || 'jirapassword',
  port: process.env.POSTGRESQL_PORT || undefined,
})

module.exports.connect = function () {
  client.connect()
}

module.exports.disconnect = function () {
  client.end()
}

module.exports.query = async function (q) {
  return await client.query(q)
}

module.exports.flush = async function (table, keys, values) {
  let vector = []
  const cs = new pgp.helpers.ColumnSet(keys, {table: table})

  for (let v = 0; v < values.length; v++) {
    if (keys.length != values[v].length) throw Error('keys length does not match values length')
    let obj = {}
    for (let i = 0; i < keys.length; i++) {
      obj[keys[i]] = values[v][i]
    }
    vector.push(obj)
  }
  
  //console.log('table:', table, 'vector:', vector)
  const query = pgp.helpers.insert(vector, cs);

  await client.none(query)
}
