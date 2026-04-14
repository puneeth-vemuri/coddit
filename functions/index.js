const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

/**
 * Trigger: when a reply's `accepted` field changes to true
 * Awards 10 bytes to the reply author.
 */
exports.onReplyAccepted = functions.firestore
  .document('posts/{postId}/replies/{replyId}')
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    
    if (!before.accepted && after.accepted) {
      const postId = context.params.postId;
      const replyAuthorUid = after.authorUid;
      
      await awardBytes(replyAuthorUid, 'REPLY_ACCEPTED', 10, 
                       `Reply accepted on post ${postId}`);
                       
      // Also mark the post as solved
      await admin.firestore().doc(`posts/${postId}`).update({
        solved: true,
        acceptedReplyId: context.params.replyId
      });
    }
  });

/**
 * Trigger: when a post's metadata changes
 * Awards bytes for upvotes and view milestones.
 */
exports.onPostMetadataUpdated = functions.firestore
  .document('posts/{postId}')
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    const authorUid = after.authorUid;
    const postId = context.params.postId;

    // Award bytes for upvotes (+2)
    if (after.upvotes > before.upvotes) {
      await awardBytes(authorUid, 'POST_UPVOTED', 2, 
                       `Post ${postId} upvoted`);
    }

    // Award bytes for view milestone (+5)
    if (before.viewCount < 100 && after.viewCount >= 100) {
      await awardBytes(authorUid, 'POST_100_VIEWS', 5, 
                       `Post ${postId} hit 100 views`);
    }
  });

/**
 * Atomic transaction to award bytes and log the event
 */
async function awardBytes(uid, action, delta, reason) {
  const db = admin.firestore();
  const userRef = db.doc(`users/${uid}`);
  const ledgerRef = db.collection(`bytes_ledger/${uid}/events`).doc();

  return db.runTransaction(async (transaction) => {
    const userDoc = await transaction.get(userRef);
    if (!userDoc.exists) return;

    transaction.update(userRef, {
      bytes: admin.firestore.FieldValue.increment(delta)
    });

    transaction.set(ledgerRef, {
      action,
      delta,
      reason,
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });
  });
}
