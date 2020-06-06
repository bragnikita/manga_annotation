const {program} = require('commander');
const {front, pixiv} = require('./operations');

program.version("0.1")
// program.name("front").usage("[ operation ]");
program.on('--help', () => {
    console.log('');
    console.log('Some additional help notes')
});
program.option('-d, --dir <directory>', 'working directory', process.cwd());
program
    .command('front')
    .description("front-end operations")
    .action(front)
    .usage("[ operation ]");
program
    .command("pixiv <artwork_id> [series_name]")
    .description("Pixiv download")
    .action(pixiv);
program.on('option:dir', (operands) => {
    process.chdir(operands)
});

program.parseAsync(process.argv);