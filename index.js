import { loadFile } from 'nbb';

await loadFile('./deps.cljs');
const { handler } = await loadFile('./lambda.cljs');

export { handler }
