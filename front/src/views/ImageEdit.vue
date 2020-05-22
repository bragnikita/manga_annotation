<template>
    <div style="height: 100vh">
        <image-editor v-if="pageLoaded"
                      :image-url="page.imageUrl" :translation="page.meta"
                      @save="saveTranslation" @nextPage="nav(1)" @prevPage="nav(-1)"
        />
    </div>
</template>

<script>
    import ImageEditor from "../components/ImageEditor";
    import {mapGetters} from 'vuex';

    export default {
        name: "ImageEdit",
        components: {
            'image-editor': ImageEditor,
        },
        data() {
            return {
                pageId: "",
            }
        },
        methods: {
            saveTranslation(meta) {
                this.$store.dispatch("savePage", {
                    pageId: this.pageId,
                    meta: meta,
                })
            },
            nav(val) {
                console.log(this.currentPageIndex, typeof this.currentPageIndex, val)
                if (val > 0 && this.currentPageIndex + 1 === this.numberOfPages) return;
                if (val < 0 && this.currentPageIndex === 0) return;
                this.changePage(this.currentPageIndex + 1 + val);
            },
            changePage(nextPageIndex) {
                const index = nextPageIndex - 1;
                const nextPage = this.$store.getters.pageByIndex(index);
                console.log(nextPageIndex, nextPage)
                if (this.currentPageId === nextPage) return;
                console.log('navigate')
                this.$router.push({
                    params: {pageId: nextPage}
                });
                this.reload();
            },
            async reload() {
                this.pageId = this.$route.params.id || this.$route.params.pageId;
                const seriesId = this.$route.params.seriesId;
                await this.$store.dispatch("loadPage", {
                    pageId: this.pageId,
                    seriesId: seriesId,
                    setCurrent: true,
                })
            }
        },
        computed: {
            ...mapGetters({page: 'currentPage'}),
            ...mapGetters(['currentPage', "currentPageIndex", "numberOfPages"]),
            pageLoaded() {
                return !!this.page && this.page.id === this.pageId
            }
        },
        created() {
           this.reload();
        },

    }
</script>

<style scoped>

</style>