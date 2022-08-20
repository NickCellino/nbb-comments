# dynamo-cljs

## nREPL in Conjure

In one tab, run:
```bash
npx shadow-cljs watch app
```

Then go to browser at localhost:8080.
Then start node with:
```bash
node dist/main.js
```

Then, start neovim and open CLJS file. Enter command:
```
:ConjureShadowSelect app
```

## nbb nRepl

```bash
npx nbb nrepl-server :port 1337
```
