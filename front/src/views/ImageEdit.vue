<template>
    <div style="height: 100vh">
        <image-editor v-if="pageLoaded"
                :image-url="page.imageUrl" :translation="page.meta"
                      @save="saveTranslation"
        />
    </div>
</template>

<script>
    import ImageEditor from "../components/ImageEditor";
    import { mapGetters} from 'vuex';

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
          }
        },
        computed: {
            ...mapGetters({page: 'currentPage'}),
            pageLoaded() {
                return !!this.page && this.page.id === this.pageId
            }
        },
        created() {
            console.log("created ")
            this.pageId = this.$route.params.id || this.$route.params.pageId;
            this.$store.dispatch("loadPage", {
                pageId: this.pageId,
                setCurrent: true,
            })
        }
    }
</script>

<style scoped>

</style>