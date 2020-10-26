import * as functions from 'firebase-functions';
import * as admin from "firebase-admin";

admin.initializeApp(functions.config().firebase);
import {calculateWeight} from "./ScoreUtils";
import {isSafe} from "./Utils";
import DocumentSnapshot = admin.firestore.DocumentSnapshot;

// Start writing Firebase Functions
// https://firebase.google.com/docs/functions/typescript

const project = process.env.GCLOUD_PROJECT;
const token = functions.config().ci_token;
const firebase_tools = require('firebase-tools');

export const investigationsListener = functions.firestore
    .document("children/{childId}")
    .onWrite(childSnapshot => {

        return admin.firestore()
            .collection("investigations")
            .get()
            .then(function (allInvestigationsSnapshot) {
                if (isSafe(childSnapshot.after) && childSnapshot.after.exists) {
                    allInvestigationsSnapshot.forEach(function (investigationSnapshot) {
                        const weight = calculateWeight(function (key) {
                            return childSnapshot.after.get(key)
                        }, function (key) {
                            return investigationSnapshot.get(key)
                        })
                        if (weight >= 0.7) {

                            const payload = {
                                data: {
                                    id: childSnapshot.after.id,
                                    weight: weight.toString()
                                }
                            };

                            admin.messaging()
                                .sendToTopic(investigationSnapshot.get("user_id"), payload)
                                .catch(function (reason) {
                                    console.log('Failed to send message: ', reason);
                                })
                        }
                    })
                }
            }, function (errorObject) {
                console.log("Failed to read investigations: " + errorObject.message);
            });
    })

export const queriesCreateListener = functions.firestore
    .document("queries/{queryId}")
    .onCreate(querySnapshot => {
        return  deleteCollection(
            "queries/" + querySnapshot.id + "/results"
        ).then(function () {
            return onQuery(querySnapshot)
        })
    })

export const queriesUpdateListener = functions.firestore
    .document("queries/{queryId}")
    .onUpdate(querySnapshot => {
        if (isSafe(querySnapshot.after) && querySnapshot.after.exists) {
            return deleteCollection(
                "queries/" + querySnapshot.after.id + "/results"
            ).then(function () {
                return onQuery(querySnapshot.after)
            })
        } else {
            return deleteCollection("queries/" + querySnapshot.before.id + "/results")
        }
    })

function onQuery(
    querySnapshot: DocumentSnapshot
): Promise<void> {

    return admin.firestore()
        .collection("children")
        .get()
        .then(function (allChildrenSnapshot) {

            let results: { child_id: string, weight: number }[] = []

            allChildrenSnapshot.forEach(function (childSnapshot) {
                const weight = calculateWeight(function (key) {
                    childSnapshot.get(key)
                }, function (key) {
                    querySnapshot.get(key)
                })
                results.push({
                    child_id: childSnapshot.id,
                    weight: weight
                })
            })

            const page = querySnapshot.get("page")

            results = results.sort(function (a, b) {
                return b.weight - a.weight
            })

            results.splice(0, 20 * page)
            results = results.slice(0, 20)

            results.forEach(function (value) {
                admin.firestore()
                    .collection("queries")
                    .doc(querySnapshot.id)
                    .collection("results")
                    .add({
                        child_id: value.child_id,
                        weight: value.weight
                    }).catch(function (reason) {
                    console.log('Failed to add query result: ', reason);
                })
            })

        }, function (errorObject) {
            console.log("Failed to read children: " + errorObject.message);
        });
}

export const queriesDeleteListener = functions.firestore
    .document("queries/{queryId}")
    .onDelete(handler => {
        return deleteCollection("queries/" + handler.id + "/results");
    })

function deleteCollection(
    path: string
): Promise<void> {
    return firebase_tools.firestore.delete(path, {
        project,
        token,
        recursive: true,
        yes: true,
    });
}
