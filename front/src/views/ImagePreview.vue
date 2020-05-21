<template>
    <div style="max-width: 1000px; display: inline-block">
        <previewer-toolbar
                @scale="changeScale"
                @visibility="displayAll=$event"
                @close="onClose"
                :current-page="currentPage"
                :number-of-pages="numberOfPages"
                :show-pager="isSeriesLoaded"
                :show-closer="showCloser"
                @page="changePage"
        />
        <image-previewer
                v-if="pageLoaded"
                :display-all="displayAll"
                :maxWidth="600"
                :scale="scale"
                :image-url="page.imageUrl"
                :translation="page.meta"
                @navigate="nav"
        />
    </div>
</template>

<script>
    import ImagePreviewer from "../components/ImagePreviewer";
    import PreviewerToolbar from "../components/PreviewerToolbar";
    import {mapGetters, mapMutations} from "vuex";

    export default {
        name: "ImagePreview",
        components: {
            'image-previewer': ImagePreviewer,
            'previewer-toolbar': PreviewerToolbar,
        },
        data() {
            return {
                seriesId: "",
                scale: 1,
                displayAll: false,
                dialogMode: false,
            }
        },
        methods: {
            ...mapMutations(['setCurrentPage']),
            nav(val) {
                if (val > 0 && this.currentPage === this.numberOfPages) return;
                if (val < 0 && this.currentPage === 1) return;
                this.changePage(this.currentPage+ val);
            },
            changeScale(newValue) {
                this.scale = newValue;
            },
            onClose() {
                if (this.isGuest) {
                    this.$router.push({name: "series_list"});
                    return;
                }
                if (this.$store.state.returnTo) {
                    this.$router.push(this.$store.state.returnTo);
                    return;
                }
                const seriesId = this.$route.params.seriesId;
                if (seriesId) {
                    this.$router.push({name: "SeriesEdit", params: {id: seriesId}})
                    return;
                }
            },
            changePage(nextPageIndex) {
                const index = nextPageIndex - 1;
                const nextPage = this.$store.getters.pageByIndex(index);
                console.log(nextPageIndex, nextPage)
                if (this.currentPageId === nextPage) return;
                console.log('navigate')
                this.$router.push({
                    params: {pageId: nextPage}
                })
            },
        },
        computed: {
            ...mapGetters({page: 'currentPage',}),
            ...mapGetters(['isSeriesLoaded', 'numberOfPages', 'currentPageIndex', "isGuest"]),
            pageLoaded() {
                return !!this.page
            },
            currentPage() {
                return this.currentPageIndex + 1;
            },
            currentPageId() {
                return this.$route.params.pageId;
            },
            showCloser() {
                return this.isSeriesLoaded
            }
        },
        created() {
            const pageId = this.currentPageId;
            this.seriesId = this.$route.params.seriesId;
            if (this.seriesId) {
                this.$store.dispatch("loadSeries", {
                    id: this.seriesId,
                    setCurrent: true,
                })
            }
            this.$store.dispatch("loadPage", {
                pageId: pageId,
                setCurrent: true,
            });
        },
        watch: {
            currentPageId(newPageId) {
                console.log("newPageId = ", newPageId)
                this.$store.dispatch("loadPage", {
                    pageId: newPageId,
                    setCurrent: true,
                });
            }
        }
    }
</script>

<style scoped>

</style>