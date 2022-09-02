# nbb-comments

This is a solution for adding comments functionality to any HTML page.

You can easily [deploy it](#deploy-your-own-instance) to your own AWS account. Or you can [play around with it running locally](#running-server-locally). Or you can see it in action [here](https://nickcellino.com/blog/2022-08-07-clojure-bandits.html) (scroll to the bottom)!

## Introduction

The backend is an AWS Lambda function that uses DynamoDB for storage. 

Once you deploy your backend, adding it to a blog page is as simple as:
```html
<!-- add this to <head> -->
<script src="https://unpkg.com/htmx.org@1.8.0" async></script>

<!-- add this somewhere in your <body> -->
<h2>Leave a comment</h2>
<form id="comment-form" hx-get="https://<your-deployed-lambda-url>/comments-form?post-id=example-post-id" hx-trigger="load"></form>

<h2>Comments</h2>
<div id="comments-list" hx-get="https://<your-deployed-lambda-url>/comments?post-id=example-post-id" hx-swap"innerHTML" hx-trigger="load"></div>
```

It is built using [nbb](https://github.com/babashka/nbb), [htmx](https://htmx.org/), AWS Lambda, Serverless Framework, and DynamoDB.

Users are allowed to leave comments anonymously, but [Google reCAPTCHA v3](https://developers.google.com/recaptcha/docs/v3) is used to provde some protection against bot abuse.


## Running server locally

##### Prerequisites
- Install [babashka](https://babashka.org/)
- Install [node/npm](https://nodejs.org/en/download/)

First, install node dependencies:
```bash
npm install
```

Then, to run the server locally, run:
```bash
bb dev-server
```

Then visit `http://localhost:3000` and you can see it in action!

## Using it on your own site

To use this for your own blog/site, you will need to:

1. [Deploy your own instance](#deploy-your-own-instance)
2. [Hook it up to your frontend](#hook-it-up-to-your-frontend)

### Deploy your own instance

#### Prerequisites
- [Setup your AWS credentials](https://www.serverless.com/framework/docs/providers/aws/guide/credentials) so that you can deploy using Serverless Framework 
- [Register a Google reCAPTCHA v3 key](https://www.google.com/recaptcha/admin/create)
- Install [node/npm](https://nodejs.org/en/download/)

1. Create an `.env` with the following contents:

    ```
    RECAPTCHA_SECRET="<your-recaptcha-secret-here>"
    RECAPTCHA_SITEKEY="<your-recaptcha-sitekey-here>"
    ALLOWED_ORIGIN_URL="<your-frontend-url>" # for example "https://nickcellino.com"
    ```

2. Run `npm install` in the root of this project.

3. Run `npx serverless deploy`. If everything worked correctly, this should print out something like:
    ```bash
    ➜  npx serverless deploy

    Deploying comments-api to stage dev (us-east-1)

    ✔ Service deployed to stack comments-api-dev (84s)

    endpoint: ANY - https://s390h072qf.execute-api.us-east-1.amazonaws.com/{proxy+}
    functions:
      comments-api: comments-api-dev-comments-api (65 MB)

    Monitor all your API routes with Serverless Console: run "serverless --console"
    ```

Take note of the endpoint url you get back. In this example, it is `https://s390h072qf.execute-api.us-east-1.amazonaws.com`

If you can see that, your backend is all set!

### Hook it up to your frontend

1. Load `htmx` script somewhere in the `<head>` of your HTML like so:
    ```html
    <head>
    ...
    <script src="https://unpkg.com/htmx.org@1.8.0" async></script>
    ...
    </head>
    ```
    This is used to dynamically fetch the comments form and comments from the backend.

2. Add the comments form somewhere on your page like so (replacing *your-backend-url* with the proper value for your backend):
    ```html
    <form
      id="comment-form"
      hx-get="<your-backend-url>//localhost:3000/comments-form?post-id=example-post-id"
      hx-trigger="load">
    </form>
    ```

3. Add the comments list (where the comemnts will actually be displayed) somewhere on your page like so (replacing *your-backend-url* with the proper value for your backend):
    ```html
    <div
      id="comments-list"
      hx-get="<your-backend-url>/comments?post-id=example-post-id"
      hx-swap"innerHTML"
      hx-trigger="load">
    </div>
    ```

Once you have done that, you are all set to receive brilliant insights from random strangers on the internet!

