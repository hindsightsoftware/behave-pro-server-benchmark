const db = require('./database')

var NEXT_FEATURE_ID = 1
var NEXT_SCENARIO_ID = 1
var NEXT_EVENT_ID = 1
var NEXT_SCENARIO_LINK_ID = 1
var NEXT_SCENARIO_SNAPSHORT_ID = 1

const FEATURE_EVENT_KEYS = ["EVENT", "EVENT_TIMESTAMP", "FEATURE", "ID", "PROJECT", "SCENARIO", "USER_KEY"]
const SCENARIO_SNAPSHOT_KEYS = ["BODY", "DELETED", "FEATURE", "ID", "PROJECT", "RANK", "REVISION", "SCENARIO"]
const SCENARIO_LINK_KEYS = ["FEATURE", "ID", "ISSUE", "PROJECT", "RANK", "SCENARIO"]

function CURDATE() {
  return new Date()
}

module.exports.writeSequences = async function () {
  await db.flush('AO_6797AA_FEATURE_SEQUENCE', ["ID"], [[NEXT_FEATURE_ID]])
  await db.flush('AO_6797AA_SCENARIO_SEQUENCE', ["ID"], [[NEXT_SCENARIO_ID]])
}

module.exports.getFeatureIds = async function () {
  let projects = await db.query('SELECT DISTINCT FEATURE FROM AO_6797AA_FEATURE_EVENT WHERE FEATURE != 0;')
  return projects.map(p => p.ID)
}

module.exports.createFeature = async function (projectId, name, description, background, tags, userKey) {
  let featureId = NEXT_FEATURE_ID++
  let values = []
  values.push([`{"feature":{"featureId":${featureId},"name":"${name}","description":null,"background":null,"addTags":[],"removeTags":[]},"action":"CREATE","scenario":null}`,CURDATE(),featureId,NEXT_EVENT_ID++,projectId,0,userKey])
  values.push([`{"feature":{"featureId":${featureId},"name":null,"description":"${description}","background":"${background}","addTags":[],"removeTags":[]},"action":"UPDATE","scenario":null}`,CURDATE(),featureId,NEXT_EVENT_ID++,projectId,0,userKey])
  values.push([`{"feature":{"featureId":${featureId},"name":null,"description":null,"background":null,"addTags":[${tags.map(t => '"' + t + '"').join(',')}],"removeTags":[]},"action":"UPDATE","scenario":null}`,CURDATE(),featureId,NEXT_EVENT_ID++,projectId,0,userKey])
  await db.flush('AO_6797AA_FEATURE_EVENT', FEATURE_EVENT_KEYS, values)

  return featureId
}

module.exports.createScenario = async function (projectId, featureId, name, steps, tags, issueIds, userKey, sessionId) {
  let scenarioId = NEXT_SCENARIO_ID++
  let values = []
  values.push([`{"feature":null,"action":"UPDATE","scenario":{"id":${scenarioId},"name":"${name}","steps":"${steps}","addTags":[],"removeTags":[],"testType":"AUTOMATIC","addIssues":[],"removeIssues":[],"addSessions":[],"removeSessions":[],"action":"CREATE","outline":false}}`,CURDATE(),featureId,NEXT_EVENT_ID++,projectId,scenarioId,userKey])
  values.push([`{"feature":null,"action":"UPDATE","scenario":{"id":${scenarioId},"name":null,"steps":null,"addTags":[${tags.map(t => '"' + t + '"').join(',')}],"removeTags":[],"testType":null,"addIssues":[],"removeIssues":[],"addSessions":[],"removeSessions":[],"action":"UPDATE","outline":null}}`,CURDATE(),featureId,NEXT_EVENT_ID++,projectId,scenarioId,userKey])
  values.push([`{"feature":null,"action":"UPDATE","scenario":{"id":${scenarioId},"name":null,"steps":null,"addTags":[],"removeTags":[],"testType":null,"addIssues":[${issueIds.join(',')}],"removeIssues":[],"addSessions":[],"removeSessions":[],"action":"UPDATE","outline":null}}`,CURDATE(),featureId,NEXT_EVENT_ID++,projectId,scenarioId,userKey])
  await db.flush('AO_6797AA_FEATURE_EVENT', FEATURE_EVENT_KEYS, values)

  let revision = NEXT_EVENT_ID - 1
  values = [[`{"id":${scenarioId},"revision":${revision},"featureId":${featureId},"name":"${name}","steps":"${steps}","tags":[${tags.map(t => '"' + t + '"').join(',')}],"testType":"AUTOMATIC","issues":[${issueIds.join(',')}],"sessions":[${sessionId ? '"' + sessionId + '"' : ''}],"outline":false}`,false,featureId,NEXT_SCENARIO_SNAPSHORT_ID++,projectId,1,revision,scenarioId]]
  await db.flush('AO_6797AA_SCENARIO_SNAPSHOT', SCENARIO_SNAPSHOT_KEYS, values)

  return scenarioId
}

module.exports.createScenarioLink = async function (projectId, featureId, scenarioId, issueIds) {
  let values = issueIds.map(issueId => [featureId,NEXT_SCENARIO_LINK_ID++,issueId,projectId,1,scenarioId])

  await db.flush('AO_6797AA_SCENARIO_LINK', SCENARIO_LINK_KEYS, values)
  return scenarioId
}
