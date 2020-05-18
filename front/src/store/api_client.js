import {default as axios} from 'axios';
import {getApiBase} from "../config";

const getClient = () => {
    return axios.create({
        baseURL: getApiBase() + "/api"
    });
};
export default getClient;