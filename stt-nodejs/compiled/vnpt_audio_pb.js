// source: vnpt_audio.proto
/**
 * @fileoverview
 * @enhanceable
 * @suppress {missingRequire} reports error on implicit type usages.
 * @suppress {messageConventions} JS Compiler reports an error if a variable or
 *     field starts with 'MSG_' and isn't a translatable message.
 * @public
 */
// GENERATED CODE -- DO NOT EDIT!
/* eslint-disable */
// @ts-nocheck

var jspb = require('google-protobuf');
var goog = jspb;
var global = (function() {
  if (this) { return this; }
  if (typeof window !== 'undefined') { return window; }
  if (typeof global !== 'undefined') { return global; }
  if (typeof self !== 'undefined') { return self; }
  return Function('return this')();
}.call(null));

goog.exportSymbol('proto.vnpt.audio.AudioEncoding', null, global);
/**
 * @enum {number}
 */
proto.vnpt.audio.AudioEncoding = {
  ENCODING_UNSPECIFIED: 0,
  LINEAR_PCM: 1,
  FLAC: 2,
  MULAW: 3,
  ALAW: 20
};

goog.object.extend(exports, proto.vnpt.audio);
