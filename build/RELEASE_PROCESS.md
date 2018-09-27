##Overview

Adobe Ride uses a branching strategy in which all new code is implemented in the "develop" branch.  From that branch certain changes are locked in releases via git tagging functionality.  If you feel that a release from a previously released version (tag) is required.  You'll need to branch from that tag and open a PR to the Adobe Ride owners requesting a new release tag.

##Versioning

Ride uses [Sematic Versioning ](https://semver.org/) for it's versioning scheme.  Develop (a.k.a alpha) releases are built with the maven "SNAPSHOT" keyword appended to the end.  There are currently no beta versions.

##Release Notification

Initially, notification of new Ride releases will only be provided to repo subscribers.  Subscribers will be notified if this changes.

