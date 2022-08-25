import { loadFile } from 'nbb';

await loadFile('./deps.cljs');
const { handler } = await loadFile('./comments/server/lambda.cljs');

export { handler }
