const functions = require('firebase-functions');
const admin = require('firebase-admin');
const cors = require('cors');
const corsHandler = cors({
  origin: true
});

admin.initializeApp();
const db = admin.firestore();

exports.crud = functions.https.onRequest((req, res) => {
  corsHandler(req, res, async () => { 
    switch(req.method) {
      case 'OPTIONS' : {
        res.status(204).send('');
      }
      case 'GET' : {
        const { collectionName } = req.query;
        try {
          const querySnapshot = await db.collection(collectionName).get();
          const results = querySnapshot.docs.map(doc => ({...doc.data(), id: doc.id}));
          res.status(200).send(results);
        } catch (error) {
          res.status(500).send(error.toString());
        }
      }
      case 'POST' : {
        const { collectionName, obj } = req.body;
        try {
          const docRef = await db.collection(collectionName).add(obj);
          res.status(200).send({ id: docRef.id });
        } catch (error) {
          res.status(500).send(error.toString());
        }
      }
      case 'PUT' : {
        const { collectionName, id, obj } = req.body;
        try {
          const docRef = db.collection(collectionName).doc(id);
          await docRef.update(obj);
          res.status(200).send({ id: docRef.id });
        } catch (error) {
          res.status(500).send(error.toString());
        }
      }
      case 'DELETE' : {
        const { collectionName, id } = req.query;
        try {
          await db.collection(collectionName).doc(id).delete();
          res.status(200).send({ result: "Document deleted successfully", id });
        } catch (error) {
          res.status(500).send(error.toString());
        }
      }
      default : {
        res.status(400).send("Bad Request");
      }
    }
  });
});
