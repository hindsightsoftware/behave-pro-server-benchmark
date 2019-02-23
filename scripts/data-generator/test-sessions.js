const db = require('./database')
const uuidv1 = require('uuid/v1')

var NEXT_SESSION_ID = 1
var NEXT_SESSION_SNAPSHOT_ID = 1

const SESSION_KEYS = ["EVENT", "EVENT_TIMESTAMP", "ID", "ISSUE", "PROJECT", "TEST_SESSION","USER_KEY"]
const SNAPSHOT_KEYS = ["BODY", "ID", "ISSUE", "PROJECT", "REVISION", "SESSION_STATE", "TEST_SESSION", "USER_KEY"]

function CURDATE() {
  return new Date()
}

module.exports.writeSequences = async function () {
  await db.query(`SELECT pg_catalog.setval(\'public.\"AO_6797AA_TEST_SESSION_EVENT_ID_seq\"\', ${NEXT_SESSION_ID}, true);`)
  await db.query(`SELECT pg_catalog.setval(\'public.\"AO_6797AA_SESSION_SNAPSHOT_ID_seq\"\', ${NEXT_SESSION_SNAPSHOT_ID}, true);`)
}

module.exports.createSession = async function (projectId, issueId, defectId, name, chart, userKey) {
  let sessionId = uuidv1()
  let timestamp = Math.floor((new Date()).getTime() / 1000)
  let values = []
  values.push([`{"action":"CREATED","delta":{"sessionId":"${sessionId}","title":"${name}","charter":"${chart}","time":45,"tester":"${userKey}","sessionState":"OPEN","activity":null,"user":"${userKey}","timestamp":${timestamp},"sessionSummary":null}}`,CURDATE(),NEXT_SESSION_ID++,issueId,projectId,sessionId,userKey])
  values.push([`{"action":"TRANSITIONED","delta":{"sessionId":"${sessionId}","title":null,"charter":null,"time":null,"tester":"${userKey}","sessionState":"WIP","activity":null,"user":"${userKey}","timestamp":${timestamp},"sessionSummary":null}}`,CURDATE(),NEXT_SESSION_ID++,issueId,projectId,sessionId,userKey])
  values.push([`{"action":"TRANSITIONED","delta":{"sessionId":"${sessionId}","title":null,"charter":null,"time":null,"tester":"${userKey}","sessionState":"PAUSED","activity":null,"user":"${userKey}","timestamp":${timestamp},"sessionSummary":null}}`,CURDATE(),NEXT_SESSION_ID++,issueId,projectId,sessionId,userKey])
  values.push([`{"action":"TRANSITIONED","delta":{"sessionId":"${sessionId}","title":null,"charter":null,"time":null,"tester":"${userKey}","sessionState":"WIP","activity":null,"user":"${userKey}","timestamp":${timestamp},"sessionSummary":null}}`,CURDATE(),NEXT_SESSION_ID++,issueId,projectId,sessionId,userKey])
  values.push([`{"action":"TRANSITIONED","delta":{"sessionId":"${sessionId}","title":null,"charter":null,"time":null,"tester":"${userKey}","sessionState":"ENDED","activity":null,"user":"${userKey}","timestamp":${timestamp},"sessionSummary":{"qualityRating":3,"coverageRating":3,"summary":"Report summary","timeMetrics":{"setup":0,"execution":0,"investigation":0}}}}`,CURDATE(),NEXT_SESSION_ID++,issueId,projectId,sessionId,userKey])
  values.push([`{"action":"UPDATED","delta":{"sessionId":"${sessionId}","title":null,"charter":null,"time":null,"tester":null,"sessionState":null,"activity":{"attachments":[],"body":"Some random comment","labels":["Ideas","Positives"],"defectId":null,"testing":null,"investigation":null,"setup":null},"user":"${userKey}","timestamp":${timestamp},"sessionSummary":null}}`,CURDATE(),NEXT_SESSION_ID++,issueId,projectId,sessionId,userKey])
  values.push([`{"action":"UPDATED","delta":{"sessionId":"${sessionId}","title":null,"charter":null,"time":null,"tester":null,"sessionState":null,"activity":{"attachments":null,"body":null,"labels":null,"defectId":${defectId},"testing":null,"investigation":null,"setup":null},"user":"${userKey}","timestamp":${timestamp},"sessionSummary":null}}`,CURDATE(),NEXT_SESSION_ID++,issueId,projectId,sessionId,userKey])
  let revision = NEXT_SESSION_ID - 1

  db.flush('AO_6797AA_TEST_SESSION_EVENT', SESSION_KEYS, values)
  
  values = [[`{"issueId":${issueId},"sessionId":"${sessionId}","title":"${name}","charter":"${chart}","time":45,"tester":"${userKey}","sessionState":"ENDED","user":"${userKey}","createdTimestamp":${timestamp},"updatedTimestamp":${timestamp},"report":{"qualityRating":3,"coverageRating":3,"summary":"Report summary","timeMetrics":{"setup":0,"execution":0,"investigation":0},"defectCount":1,"timestamp":${timestamp},"user":"${userKey}"},"activities":[{"attachments":null,"body":null,"labels":null,"defectId":null,"transition":"WIP","user":"${userKey}","timestamp":${timestamp}},{"attachments":null,"body":null,"labels":null,"defectId":null,"transition":"PAUSED","user":"${userKey}","timestamp":${timestamp}},{"attachments":null,"body":null,"labels":null,"defectId":null,"transition":"WIP","user":"${userKey}","timestamp":${timestamp}},{"attachments":null,"body":null,"labels":null,"defectId":null,"transition":"ENDED","user":"${userKey}","timestamp":${timestamp}}],"version":5}`,NEXT_SESSION_SNAPSHOT_ID++,issueId,projectId,revision,'ENDED',sessionId,userKey]]
  db.flush('AO_6797AA_SESSION_SNAPSHOT', SNAPSHOT_KEYS, values)

  return sessionId
}
