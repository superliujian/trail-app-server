var ops = {};

function addComment(querystring) {
  console.log("Added comment");
}
ops.add_comment = addComment;

function get(opname) {
  return ops[opname];
}
exports.get = get;