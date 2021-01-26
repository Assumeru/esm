# Topic Overlap Checker

Attempts to aid modders find overlapping topics which can break quests in other content files.

## Problem

Morrowind automatically adds topics to the player's list of known topics if those topics feature in dialogue.
To determine if a topic should be added, the longest matching topic out of all existing topics is evaluated to see if the actor has any responses for it.
If so, the topic is turned into a link and added to the player's list.

This simple selection algorithm means it is possible to block off topics created by (other) mods and the base game.
For example, Morrowind.esm adds the topic `work` and relies on Larrius Varro's greeting of "I've got some work for you, if you're interested." to add it.
Tribunal.esm adds the topic `some work` for another quest. Varro's greeting matches both,
but because `some work` is longer than `work` it ends up being selected despite Varro not having any responses for the topic.
This makes it impossible for the player to continue Larrius Varro's quest unless they've gotten the `work` topic some other way
(for example by using the `AddTopic` command.)

## Usage

`java -jar topic-overlap-checker.jar content-file-1.esm content-file2.esp`
