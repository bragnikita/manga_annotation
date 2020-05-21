import Vue from 'vue'
import Vuex from 'vuex'
import getClient, {setAccessToken} from "./api_client";

Vue.use(Vuex);

export default new Vuex.Store({
    state: {
        loading: false,
        listing: [],
        currentSeries: undefined,
        currentPage: undefined,
        pagesMap: {},
        pagesSequence: [],
        returnTo: undefined,
        authenticated: false,
        user: null,
        error: "",
        errorChanged: 0,
    },
    mutations: {
        loading(state, value) {
            state.loading = value;
        },
        listing(state, items) {
            state.listing = items || [];
        },
        addPage(state, page) {
            state.pagesMap[page.id] = page;
        },
        setCurrentPage(state, {id}) {
            state.currentPage = state.pagesMap[id]
        },
        updatePage(state, {id, meta}) {
            const page = state.pagesMap[id];
            page.meta = meta;
        },
        setCurrentSeries(state, series) {
            state.currentSeries = series;
            state.pagesSequence = series.pages.map((p) => {
                return p.id
            });
            if (state.currentPage) {
                const id = state.currentPage.id;
                if (!state.pagesSequence.find(v => v === id)) {
                    state.currentPage = undefined;
                }
            }
        },
        setReturnTo(state, returnTo) {
            state.returnTo = returnTo;
        },
        removePage(state, { id }) {
            if (state.pagesSequence) {
                const index = state.pagesSequence.findIndex((pid) => pid === id)
                state.pagesSequence = state.pagesSequence.splice(index, 1)
            }
            delete state.pagesMap[id]
        },
        pageToCollection(state, pageId) {
            state.pagesSequence.push(pageId);
        },
        sortPagesFilename(state) {
            state.pagesSequence.sort();
        },
        setUser(state, user) {
            state.authenticated = true;
            state.user = user;
        },
        signOut(state) {
            state.authenticated = false;
            state.user = null;
        },
        setGlobalError(state, message) {
            state.error = message;
            state.errorChanged = new Date().getTime();
        }
    },
    actions: {
        loadListing({commit}) {
            commit('loading', true);

            getClient().get("/series", {})
                .then(({data}) => commit('listing', data.items))
                .finally(() => commit('loading', false))
        },
        uploadPage({getters, commit}, {file, seriesId}) {
            const id = seriesId || getters.currentSeriesId;
            if (!id) return;

            const form = new FormData();
            form.append("file", file);
            return getClient().post(`/series/${id}/pages`, form, {
                headers: {'Content-Type': 'multipart/form-data'}
            }).then(({data}) => {
                commit('pageToCollection', data.id);
                return data;
            });
        },
        saveSeries({}, {id, info, pages}) {
            if (id) {
                return getClient().put(`/series/${id}`, {info, pages})
            } else {
                return getClient().post("/series", {info})
                    .then(({data}) => data)
            }
        },
        loadSeries({commit, getters}, {id, setCurrent = false} = {}) {
            if (!id) return;
            // if (getters.currentSeriesId === id) return;
            return getClient().get(`/series/${id}`).then(({data}) => {
                if (setCurrent) {
                    commit('setCurrentSeries', data)
                }
                return data;
            })
        },
        deletePage({commit}, {id}) {
            commit('removePage', {id});
            return getClient().delete(`/pages/${id}`)
        },
        loadPage({getters, commit}, {pageId, setCurrent = false} = {}) {
            // if (getters.pageById(pageId)) {
            //     if (setCurrent) {
            //         commit('setCurrentPage', {id: pageId});
            //     }
            //     return;
            // }
            return getClient().get(`/pages/${pageId}`).then(({data}) => {
                commit('addPage', {
                    id: data.id,
                    imageUrl: data.imageUrl,
                    meta: data.meta,
                });
                if (setCurrent) {
                    commit('setCurrentPage', {id: data.id})
                }
            })
        },
        savePage({commit, getters, state}, {pageId, meta}) {
            const id = pageId || this.currentPageId;
            if (!id) return;
            commit('updatePage', {id: pageId, meta: meta});
            return getClient().put(`/pages/${id}`, {meta})
        },
        savePagesOrder({getters}) {
            const pages = getters.pagesList.map((id) => {
                return id.split("__").slice(1).join("__")
            });
            console.log(pages);
            return getClient().put(`/series/${getters.currentSeriesId}`, {pages})
        },
        async signIn({ commit }, credentials) {
            const res = await getClient("").post(`/login`, credentials, {
                 validateStatus: status => {
                     return status < 500
                 }
            });
            if (res.status !== 401) {
                console.log(res.headers);
                const token = res.headers['authorization'];
                setAccessToken(token);
                commit('setUser', { username: credentials.username });
                window.localStorage.setItem("ma:access_token", token);
            } else {
                commit('setGlobalError', "Username/password pair is incorrect")
            }
        },
        signOut({ commit }) {
            setAccessToken(null);
            window.localStorage.removeItem("ma:access_token");
            commit('signOut')
        }
    },
    getters: {
        currentSeriesId: state => state.currentSeries ? state.currentSeries.info.id : null,
        pageById: (state) => (id = "") => state.pagesMap[id],
        pageByIndex: (state, getters) => (index) => {
            return state.pagesSequence[index];
        },
        currentPage: state => state.currentPage,
        currentPageId: state => state.currentPage ? state.currentPage.id : null,
        isSeriesLoaded: (state, getters) => !!getters.currentSeriesId,
        currentPageIndex: (state, getters) => {
            if (state.currentPage) {
                return state.pagesSequence.indexOf(state.currentPage.id)
            } else {
                return -1;
            }
        },
        numberOfPages: (state) => state.pagesSequence.length,
        firstPageOfSeries: (state) => {
            return state.pagesSequence[0]
        },
        hasPages(state, getters) {
            return !!getters.firstPageOfSeries
        },
        pagesList(state) {
            return state.pagesSequence;
        },
        user: (state) => state.user,
        isAuthenticated: (state) => state.authenticated,
        isAdmin: state => state.user && state.user.username === 'admin',
        isEditor: (state, getters) => getters.isAdmin,
        isGuest: (state,getters) => !getters.isAuthenticated,
        hasError: (state) => !!state.error,
        error: (state) => state.error,
        lastErrorTime: (state) => state.errorChanged,
    },
    modules: {}
})
