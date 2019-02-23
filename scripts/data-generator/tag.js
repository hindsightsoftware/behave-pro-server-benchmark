const db = require('./database')

var NEXT_TAG_ID = 1

const KEYS = ["ID", "PROJECT", "TAG", "TAGL"]

module.exports.writeSequences = async function () {
  await db.query(`SELECT pg_catalog.setval(\'public.\"AO_6797AA_TAG_SNAPSHOT_ID_seq\"\', ${NEXT_TAG_ID}, true);`)
}

module.exports.addTagsSnapshot = async function (projectId, tags) {
  let values = tags.map(tag => [NEXT_TAG_ID++,projectId,tag,tag.toLowerCase()])
  await db.flush('AO_6797AA_TAG_SNAPSHOT', KEYS, values)
}
