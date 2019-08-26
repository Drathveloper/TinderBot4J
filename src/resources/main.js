#!/usr/bin/env node

const puppeteer = require('puppeteer');
const fs = require('fs');
const argv = require('minimist')(process.argv.slice(2));
const path = argv.path;
const user = argv.user;
const pass = argv.pass;
(async() => {
    if(path && user && pass){
        let token = undefined;
        const browser = await puppeteer.launch();
        const page = await browser.newPage();
        await page.goto('https://www.facebook.com/login.php?skip_api_login=1&api_key=464891386855067&kid_directed_site=0&app_id=464891386855067&signed_next=1&next=https%3A%2F%2Fwww.facebook.com%2Fv2.6%2Fdialog%2Foauth%3Fredirect_uri%3Dfb464891386855067%253A%252F%252Fauthorize%252F%26scope%3Duser_birthday%252Cuser_photos%252Cuser_education_history%252Cemail%252Cuser_relationship_details%252Cuser_friends%252Cuser_work_history%252Cuser_likes%26response_type%3Dtoken%252Csigned_request%26client_id%3D464891386855067%26ret%3Dlogin%26fallback_redirect_uri%3D221e1158-f2e9-1452-1a05-8983f99f7d6e%26fbapp_pres%3D0%26logger_id%3D676295a3-8faa-4135-9e5f-a5b15e60b764&cancel_url=fb464891386855067%3A%2F%2Fauthorize%2F%3Ferror%3Daccess_denied%26error_code%3D200%26error_description%3DPermissions%2Berror%26error_reason%3Duser_denied%23_%3D_&display=page&locale=es_ES');
        await page.waitFor(3000);
        await page.type('#email', user);
        await page.type('#pass', pass);
        await page.waitFor(1500);
        await page.click('#loginbutton');
        await page.waitForNavigation();
        try {
            await page.waitFor(2000);
            await page.$eval('#platformDialogForm', form => {
                form.submit();
            });
            await page.waitForSelector('script');
            const fullResponse = await page.evaluate(() => {
                return document.querySelector('script').textContent;
            });
            let token = fullResponse.split('&').filter((part) => {
                return part.includes('access_token');
            })[0];
            if(token != undefined){
                fs.writeFile(path + "fbtoken.properties", token, (err) => {
                    if(err) return console.log(err);
                });
                console.log('Token saved');
            }
            console.log(token);
        } catch(e){
            console.log("Wrong user/password")
            process.exit(1);
        }
        await browser.close();
    } else {
        console.log("Wrong parameters");
        process.exit(1);
    }
    process.exit(0);
})();