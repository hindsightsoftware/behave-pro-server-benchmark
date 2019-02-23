const db = require('./database')
const Project = require('./project')
const ProjectSettings = require('./project-settings')
const Issue = require('./issue')
const Tables = require('./tables')
const Feature = require('./feature')
const User = require('./user')
const TestSessions = require('./test-sessions')
const Tag = require('./tag')

const FEATURES_PER_PROJECT = 20
const SCENARIOS_PER_FEATURE = 5
const QUESTIONS_PER_ISSUE = 5
const APPROVALS_PER_ISSUE = 5

function random () {
  return Math.random().toString(36).substring(2)
}

function pick (items) {
  return items[Math.floor(Math.random() * items.length)]
}

async function main () {
  console.log('Connecting to the database')
  await db.connect()

  console.log('Creating Behave Pro tables')
  await Tables.createIfNotExist()

  let projectIds = await Project.getProjectIds()
  let userKeys = await User.getUserKeys()
  console.log('Found', projectIds.length, 'projects!')
  console.log('Found', userKeys.length, 'users!')

  console.log('Creating', projectIds.length, 'project settings...')
  for (let projectId of projectIds) {
    await ProjectSettings.create(projectId)
  }

  for (let i = 0; i < projectIds.length; i++) {
    console.log('' + (projectIds.length - i), 'projects left to do!')
    const projectId = projectIds[i]

    const issueIds = await Issue.getIssueIds(projectId)
    console.log('Found', issueIds.length, 'issues in project', projectId)

    console.log('Creating', FEATURES_PER_PROJECT, 'features for project', projectId, '...')
    for (let i = 0; i < FEATURES_PER_PROJECT; i++) {
      let tags = [random().toUpperCase(), random().toUpperCase()]
      let featureId = await Feature.createFeature(
        projectId,
        'Feature ' + random(),
        'Random description',
        'Given some background',
        tags,
        'admin'
      )
      await Tag.addTagsSnapshot(projectId, tags)

      // Create scenarios for the last created feature
      console.log('Creating', SCENARIOS_PER_FEATURE, 'scenarios for feature', featureId, 'project', projectId, '...')
      for (let t = 0; t < SCENARIOS_PER_FEATURE; t++) {
        let issues = [pick(issueIds), pick(issueIds)]
        let tags = [random().toUpperCase(), random().toUpperCase()]
        let scenarioId = await Feature.createScenario(
          projectId,
          featureId,
          'Scenario ' + random(),
          'Given some steps and some context',
          tags,
          issues,
          'admin',
          null
        )
        await Feature.createScenarioLink(projectId, featureId, scenarioId, issueIds)
        await Tag.addTagsSnapshot(projectId, tags)
      }
    }

    await Feature.writeSequences()
    await Tag.writeSequences()

    // Create behave issue for each linked issue
    console.log('Creating', issueIds.length, 'behave issues for project', projectId, '...')
    await Issue.createBehaveIssues(projectId, issueIds)
    let behaveIssueIds = await Issue.getBehaveIssueIds(projectId)

    // Create questions and approvals for each issue
    console.log('Creating', QUESTIONS_PER_ISSUE, 'questions and', APPROVALS_PER_ISSUE, 'approvals for', behaveIssueIds.length, 'issues in project', projectId)
    for (let issueId of behaveIssueIds) {
      // Questions and Approvals use FOREIGN KEY (`ISSUE_ID`) REFERENCES `AO_6797AA_ISSUES` (`ID`))
      // which is auto incrementing!
      // We can safely assume there is exactly len(issueIds.length) AO_6797AA_ISSUES issues!
      let questions = []
      let questionUserKeys = []
      let approvalUserKeys = []
      for (let i = 0; i < QUESTIONS_PER_ISSUE; i++) {
        questions.push('Lorem ipsum donor')
        questionUserKeys.push(pick(userKeys))
      }
      for (let i = 0; i < APPROVALS_PER_ISSUE; i++) {
        approvalUserKeys.push(pick(userKeys))
      }
      await Issue.createQuestions(projectId, issueId, questions, questionUserKeys)
      await Issue.createApprovals(projectId, issueId, approvalUserKeys)
    }

    await Issue.writeSequences()

    // Create test sessions
    console.log('Creating', issueIds.length, 'test sessions...')
    for (let issueId of issueIds) {
      await TestSessions.createSession(projectId, issueId, pick(issueIds), 'Test Session ' + random(), 'Some random chart', pick(userKeys))
    }

    await TestSessions.writeSequences()
  }
}

(async () => {
  try {
    await main()
    process.exit(0)
  } catch (e) {
    console.error(e)
    process.exit(1)
  }
})()
