const db = require('./database')

var ENTITY_PROPERTY_ID = 10001
const PROJECT_SETTINGS_KEYS = ['API_KEY', 'GHERKIN_LANG', 'ID', 'PROJECT_DISABLED', 'SCHEMA_VERSION', 'TEST_TYPE_DEFAULT']
const ENTITY_PROPERTY_KEYS = ['id', 'entity_name', 'entity_id', 'property_key', 'created', 'updated', 'json_value']

function CURDATE() {
  return new Date()
}

module.exports.getProjectIds = async function () {
  let projects = await db.query('SELECT ID FROM AO_6797AA_PROJECT_SETTINGS;')
  return projects.map(p => p.ID)
}

module.exports.create = async function (projectId) {
  let values = [['me77gtz9lvvsqc0korsep5dcqdjln9dxfytr','EN',projectId,false,3,'AUTOMATIC']]
  await db.flush('AO_6797AA_PROJECT_SETTINGS', PROJECT_SETTINGS_KEYS, values)

  //values = [[ENTITY_PROPERTY_ID++,'ProjectProperty',projectId,'disableBehavePro',CURDATE(),CURDATE(),'false']]
  //await db.flush('entity_property', ENTITY_PROPERTY_KEYS, values)
}
