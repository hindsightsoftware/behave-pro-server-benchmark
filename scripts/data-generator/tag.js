const db = require('./database')

var NEXT_TAG_ID = 1

const KEYS = ["ID", "PROJECT", "TAG", "TAGL"]

module.exports.addTagsSnapshot = async function (projectId, tags) {
  let values = tags.map(tag => [NEXT_TAG_ID++,projectId,tag,tag.toLowerCase()])
  await db.flush('AO_6797AA_TAG_SNAPSHOT', KEYS, values)
}
