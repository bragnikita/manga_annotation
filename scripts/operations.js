const fs = require("fs");
const inquirer = require('inquirer');
const ora = require('ora');
const execa = require('execa');
const {Readable} = require('stream');

const front = async (operation) => {
    console.log(fs.readdirSync("."))
};

const getMeta = async (id) => {
    const {stdout, failed, exitCode} = await execa('curl', [`https://www.pixiv.net/ajax/illust/${id}/pages`]);
    if (failed || exitCode !== 0) {
        throw "Failed"
    } else {
        const response = JSON.parse(stdout);
        if (response.error) {
            throw "Failed"
        } else {
            return response;
        }
    }
};

const pixiv = async (id, series) => {
    if (!series) {
        const prompt = await inquirer.prompt([{
            type: 'input',
            name: 'seriesName',
            message: 'Setup pages file name',
            default: id,
            validate: (value) => {
                return !!value.match(/^[a-zA-Z_()\[\].]+$/)
            }
        }]);
        series = prompt.seriesName;
    }
    const dir = "utils/pixiv/" + series;

    fs.mkdirSync(dir, {recursive: true});

    let spinner = ora({text: 'Fetching series list'}).start();
    let json;
    try {
        json = await getMeta(id);
        spinner.succeed("Series list fetched")
    } catch (e) {
        spinner.fail(e);
        return;
    }
    const oldDir = process.cwd();
    spinner = ora({text: 'Loading series'}).start();
    try {
        process.chdir("utils/pixiv");
        const pd = execa('node', ['pixiv_downloader_pipe.js', id, series]);
        const r = Readable.from([JSON.stringify(json)]);
        r.pipe(pd.stdin);

        const curl = execa('curl', ['-K', '-']);
        pd.stdout.pipe(curl.stdin);

        const result1 = await pd;
        const result2 = await curl;

        spinner.succeed("Download finished!");
    } catch (e) {
        spinner.fail("Download failed");
        console.log(e);
    } finally {
        process.chdir(oldDir);
    }
};

module.exports = {
    front, pixiv
};