import { initializeApp, cert, applicationDefault } from 'firebase-admin';
import { getFirestore, Timestamp, FieldPath, DocumentSnapshot } from 'firebase-admin/firestore';
import { getMessaging } from 'firebase-admin/messaging';

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
 *
 * @param {string} collection
 * @param {Iterable<string>} ids
 */
async function docsInSet(collection, ids) {
    /**
     *
     * @param {Iterable<string>} arr
     * @param {number} size
     * @returns {string[][]}
     */
    const chunks = (arr, size) =>
        Array.from({ length: Math.ceil(arr.length / size) }, (_, i) => arr.slice(i * size, (i + 1) * size));

    const snapshots = await Promise.all(
        chunks(ids, 30).map((batch) => db.collection(collection).where(FieldPath.documentId(), 'in', batch).get()),
    );

    return snapshots.flatMap((snapshot) => snapshot.docs);
}

/**
 *
 * @param {DocumentSnapshot} notif
 */
async function sendNotif(notif) {
    try {
        await messaging.sendEachForMulticast({
            fids: (await docsInSet('entrants', notif.get('receivers'))).map((doc) => doc.get('notificationId')),
            notification: {
                title: notif.get('title'),
                body: notif.get('body'),
            },
        });
        notif.ref.update({ sent: true });
    } catch (error) {
        console.error(error);
        // TODO: retry?
    }
}

/**
 * Draws every lottery that is due.
 *
 * @param now: A timestamp of now
 * @returns an array of the lottery IDs
 */
async function drawDueLotteries(now) {
    // Disclosure: I asked Claude Opus 5.5 for help with the JavaScript firebase admin sdk syntax
    // I am very familiar with JavaScript as a language but I've never used firebase in it
    // So I asked it prompts like "in JS firebase-admin, how can I get all values for a collection where a timestamp column is before now"
    // It helped me with the syntax of the below, and how to use runTransaction, but the logic of picking winners is my own.
    const due = await db.collection('lotteries').where('lotteryEndTime', '<=', now).get();
    const drawn = [];

    for (const doc of due.docs) {
        if ((doc.get('winnerIds') ?? []).length > 0) continue;

        /**
         * @type {string[]}
         */
        const winners = await db.runTransaction(async (tx) => {
            const snap = await tx.get(doc.ref);
            if (!snap.exists || (snap.get('winnerIds') ?? []).length > 0) return [];

            const winners = pickWinners(snap.get('entrantIds') ?? [], snap.get('numberOfWinners') ?? 1);
            if (winners.length > 0) tx.update(doc.ref, { winnerIds: winners });
            return winners;
        });

        const campNames = (await docsInSet('campsites', doc.get('campsiteIds')))
            .map((doc) => doc.get('name'))
            .join(', ');

        const winNotif = db.collection('notifications').doc();
        if (winners.length>0) {
            winNotif.create({
                title: 'You won!',
                body: `You won the lottery for ${campNames}. Open the app to reserve your stay`,
                id: winNotif.id, // i dont think we actually use the id field, its just a result of lazy encoding in the app
                sender: 'SYSTEM', // i dont know if a fake user id here breaks anything, and it might be reasonable to use the owner id
                receivers: winners,
                sent: false,
            });            
        }

        /**
         * @type {String[]}
         */
        const allIds = doc.get('entrantIds');
        const winnersSet = new Set(winners);
        const losers = allIds.filter((id) => !winnersSet.has(id));
        const loseNotif = db.collection('notifications').doc();
        if (losers.length>0) {
            loseNotif.create({
                title: 'You lost :(',
                body: `You lost the lottery for ${campNames}. Better luck next time`,
                id: loseNotif.id,
                sender: 'SYSTEM',
                receivers: losers,
                sent: false,
            });
        }

        drawn.push(doc.id);
        console.log(`Drew lottery ${doc.id}`);
    }

    return drawn;
}

async function processUnsentNotifs() {
    const notifs = await db.collection('notifications').where('sent', '==', false).get();

    for (const notif of notifs.docs) {
        sendNotif(notif);
    }
}

const raw = process.env.FIREBASE_SERVICE_ACCOUNT;
applicationDefault;
initializeApp({ credential: cert(JSON.parse(raw)) });
const db = getFirestore();
const messaging = getMessaging();

if (!raw) {
    throw new Error('No Firebase Account');
}

const ids = await drawDueLotteries(Timestamp.now());
console.log(`Done, drew ${ids.length} ${ids.length === 1 ? 'lottery' : 'lotteries'}`);

processUnsentNotifs();

// module.exports = { pickWinners, drawDueLotteries };
