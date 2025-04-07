## AQLabs SCM Repository Hygiene Tool

### Why?
The reason why I have created such a tool is to help keep clean as I develop. 
I get too caught up in creating code and merging it that I forget to clean up after my self. 
In real life I keep my house clean but since there is no automation that can do that I must do it the manual way.
What is different here I can automate the cleanup and why not put my coding skills to clean up after me.

### Features?
Currently, it can scan one GitHub organization but with alil bit more tweaking it can be extended for a larger organization.

The Hygiene tool can:
- find PRs
- find old branches
- remove branches
- and close PRs
- send a report via Discord on what was cleaned.

### How to use?
To use the Janitor is pretty simple giving that you already sorted out how to deploy. 
The Janitor uses the application.properties file to set up the beans it needs to run

in the `application.properties` you will find the following (Yes it needs all these properties to work):
- `schedule.org.id` its function is to provide the tool what org to scan.
- `sweeper.branch.dryRun` true prevents the janitor from deleting branches, false will remove branches.
- `sweeper.branch.delete.days` threshold of how old the branches are deem-able to delete.
- `sweeper.pullRequest.dryRun` true prevent the janitor from closing PRs, false will close PRs.
- `sweeper.pullRequest.delete.days` threshold of how old the PRs are deem-able to close.
- `sweeper.ignore.branches` branches you want to ignore typically main,develop else is fair game.
- `sweeper.api.key` environment variable to supply your GitHub Api-key.
- `sweeper.discord.notify.endpoint` discord webhook endpoint where the janitor will deliver its report.
- `sweeper.schedule` typical cron job syntax to set your schedule for the purging to happen

Once all of these properties are defined in the application you can go ahead and deploy or start locally.

Once the application is up and running...you can now wait for the scheduler to run its task or if you are 
impatient or need to test something you can manually trigger the run by sending a 

GET -> `http://<addressOfJanitor>:9001/api/v1/janitor/run`

If the run was successful a 200 ok response is sent to the client
if some funky thing occurs the server will send a 500 to the client, check logs for what happened