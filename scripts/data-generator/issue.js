const db = require('./database')

var NEXT_ISSUE_ID = 1
var NEXT_QUESTION_ID = 1
var NEXT_APPROVAL_ID = 1

const ISSUE_KEYS = ["ID", "ISSUE", "PROJECT"]
const QUESTION_KEYS = ["ID", "ISSUE_ID", "QUESTION", "QUESTION_TIMESTAMP", "RESOLVED", "USER_KEY"]
const APPROVAL_KEYS = ["APPROVAL_TIMESTAMP", "APPROVED", "ID", "ISSUE_ID", "USER_KEY"]

function CURDATE() {
  return new Date()
}

module.exports.getIssueIds = async function (projectId) {
  let issues = await db.query(`SELECT ID FROM jiraissue where PROJECT=${projectId};`)
  return issues.map(i => parseInt(i.id))
}

module.exports.createBehaveIssue = async function (projectId, issueId) {
  let values = [[NEXT_ISSUE_ID++,issueId,projectId]]
  await db.flush('AO_6797AA_ISSUES', ISSUE_KEYS, values)
}

module.exports.createBehaveIssues = async function (projectId, issueIds) {
  let values = issueIds.map(issueId => [NEXT_ISSUE_ID++,issueId,projectId])
  await db.flush('AO_6797AA_ISSUES', ISSUE_KEYS, values)
}

module.exports.createQuestions = async function (projectId, issueId, texts, userKeys) {
  let values = []
  for (let i = 0; i < texts.length; i++) {
    values.push([NEXT_QUESTION_ID++,issueId,texts[i],CURDATE(),false,userKeys[i]])
  }
  await db.flush('AO_6797AA_QUESTION_ENTITY', QUESTION_KEYS, values)
}

module.exports.createApprovals = async function (projectId, issueId, userKeys) {
  let values = userKeys.map(userKey => [CURDATE(),true,NEXT_APPROVAL_ID++,issueId,userKey])
  await db.flush('AO_6797AA_APPROVAL_ENTITY', APPROVAL_KEYS, values)
}
