# ktm-jitpack-notifier

Backend service which makes integration with ktm easier and seamless

JitPack api returns only results for repositories which have at least one git tag and that tag has been requested to download.
Which is quite inconvenient because any new verion of artifact won't be shown in search results unless someone ask jitpack for it directly.

That is the place very ktm-jitpack-notifier comes handy.
To enable ktm-jitpack-notifier all you have to do is to add it as a webhook to your repository.
To do this go to settings->webhooks->add webhook and paste following url:

`https://morning-springs-55143.herokuapp.com/webhook`

From now on whenever you push a new commit or new tag ktm-jitpack-notifier will get a hook from github and will requst jitpack to build a new artifact.
