import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import ImageEdit from "../views/ImageEdit";
import ImagePreview from "../views/ImagePreview";
import SeriesList from "../views/SeriesList";
import SeriesCreateEdit from "../views/SeriesCreateEdit";

Vue.use(VueRouter);

const routes = [
    {
        path: "/series",
        name: 'series_list',
        component: SeriesList,
    },
    {
        path: '/about',
        name: 'About',
        // route level code-splitting
        // this generates a separate chunk (about.[hash].js) for this route
        // which is lazy-loaded when the route is visited.
        component: () => import(/* webpackChunkName: "about" */ '../views/About.vue')
    },
    {
        path: '/i/:id',
        name: 'ImageEdit',
        component: ImageEdit,
    },
    {
        path: '/i/:seriesId/:pageId',
        name: 'ImageEditWithSeries',
        component: ImageEdit,
    },
    {
        path: '/p/:pageId',
        name: 'ImagePreview',
        component: ImagePreview,
    },
    {
        path: '/p/:seriesId/:pageId',
        name: 'ImagePreviewWithSeries',
        component: ImagePreview,
    },
    {
        path: '/s/new',
        name: 'SeriesCreate',
        component: SeriesCreateEdit
    },
    {
        path: '/s/:id',
        name: 'SeriesEdit',
        component: SeriesCreateEdit
    },
];

const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
})

export default router
