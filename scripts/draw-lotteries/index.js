// I attempted to mimic KDocs for this

// Uses the firebase admin javascript sdk to get all of the lotteries that have ended
// Filters to the ones that don't have any winners yet and should have them (so lotteries with 0 entrants shouldn't have any winners)
// And then draws them and sets the properties

/**
 * Picks up to n IDs at random, using a Fisher-Yates shuffle.
 *
 * @param ids IDs to pick from
 * @param n How many to pick
 * @returns the winner ids
 */
function pickWinners(ids, n) {
  const shuffled = [...ids];
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  return shuffled.slice(0, Math.max(0, n));
}

/**
 * Draws every lottery that is due.
 *
 * @param db: The Firestore db
 * @param now: A timestamp of now
 * @returns an array of the lottery IDs
 */
async function drawDueLotteries(db, now) {
  // Disclosure: I asked Claude Opus 5.5 for help with the JavaScript firebase admin sdk syntax
  // I am very familiar with JavaScript as a language but I've never used firebase in it
  // So I asked it prompts like "in JS firebase-admin, how can I get all values for a collection where a timestamp column is before now"
  // It helped me with the syntax of the below, and how to use runTransaction, but the logic of picking winners is my own.
  const due = await db
    .collection("lotteries")
    .where("lotteryEndTime", "<=", now)
    .get();
  const drawn = [];

  for (const doc of due.docs) {
    if ((doc.get("winnerIds") ?? []).length > 0) continue;

    const didDraw = await db.runTransaction(async (tx) => {
      const snap = await tx.get(doc.ref);
      if (!snap.exists || (snap.get("winnerIds") ?? []).length > 0)
        return false;

      const winners = pickWinners(
        snap.get("entrantIds") ?? [],
        snap.get("numberOfWinners") ?? 1,
      );
      if (winners.length === 0) return false; // Nobody entered, so there's nothing to draw

      tx.update(doc.ref, { winnerIds: winners });
      return true;
    });

    if (didDraw) {
      drawn.push(doc.id);
      console.log(`Drew lottery ${doc.id}`);
    }
  }

  return drawn;
}

async function main() {
  const raw = process.env.FIREBASE_SERVICE_ACCOUNT;
  if (!raw) {
    throw new Error("No Firebase Account");
  }

  const { initializeApp, cert } = require("firebase-admin/app");
  const { getFirestore, Timestamp } = require("firebase-admin/firestore");

  initializeApp({ credential: cert(JSON.parse(raw)) });
  const ids = await drawDueLotteries(getFirestore(), Timestamp.now());
  console.log(
    `Done, drew ${ids.length} ${ids.length === 1 ? "lottery" : "lotteries"}`,
  );
}

if (require.main === module) {
  main().catch((e) => {
    console.error(e);
    process.exit(1);
  });
}

module.exports = { pickWinners, drawDueLotteries };
