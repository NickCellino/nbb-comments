import { loadFile } from 'nbb';

await loadFile('./deps.cljs');
const { handler } = await loadFile('./src/lambda.cljs');

export { handler }
