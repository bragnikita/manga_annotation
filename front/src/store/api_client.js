import {default as axios} from 'axios';
import {getApiBase} from "../config";

let accessToken = null;


const getClient = (prefix = "/api") => {
    const headers = {};
    if (accessToken) {
        headers['Authorization'] = accessToken;
    }
    return axios.create({
        baseURL: getApiBase() + prefix,
        headers,
    });
};

export const setAccessToken = (tokenStr) => { accessToken = tokenStr };

export default getClient;