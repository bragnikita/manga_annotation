<template>
    <fragment>
        <v-app-bar dense fl>
            <v-toolbar-title>
                <v-toolbar-title>My Otakumole</v-toolbar-title>
            </v-toolbar-title>
            <v-spacer></v-spacer>
            <v-btn icon @click="$router.push({name: 'series_list'})">
                <v-icon>list</v-icon>
            </v-btn>
        </v-app-bar>
        <v-container style="max-width: 800px; margin: auto">
            <v-row>
                <v-col sm="12"
                >
                    <v-card class="" style="">
                        <v-card-title>Series info</v-card-title>
                        <v-card-text>
                            <v-form v-model="valid" :lazy-validation="true" ref="form">
                                <v-text-field v-model="title" label="Title" :rules="titleRules"></v-text-field>
                                <v-textarea v-model="description" label="Descripion" auto-grow rows="2"></v-textarea>
                            </v-form>
                            <div v-if="hasCover" class="py-2 d-flex flex-column align-start">
                                <div class="pb-2">Cover</div>
                                <v-img height="300" contain :src="cover.previewUrl" max-width="400"/>
                            </div>
                        </v-card-text>
                        <v-card-actions>
                            <v-btn color="success" :disabled="!valid" @click="save" :loading="loading">Save</v-btn>
                            <v-btn :disabled="!hasPages" @click="startReading">Read</v-btn>
                        </v-card-actions>
                    </v-card>
                </v-col>
            </v-row>
            <v-row>
                <v-col sm="12">
                    <v-card v-if="canUpload">
                        <v-card-title class="d-flex justify-space-between">
                            <span>Pages (total {{ pages.length }})</span>
                            <div v-if="sortDialog">
                                <v-btn color="green"  class="mr-4" @click="commitManualSort">Confirm</v-btn>
                                <v-btn color="red" @click="sortDialog = false">Cancel</v-btn>
                            </div>
                            <div v-else>
                                <v-btn @click="sortPagesManual" class="mr-4">Manual sort</v-btn>
                                <v-btn @click="sortPages">Sort by filename</v-btn>
                            </div>
                        </v-card-title>
                        <v-card-text v-if="sortDialog">
                            <sorter v-model="sortableItems" />
                        </v-card-text>
                        <v-card-text v-else>
                            <v-container fluid>
                                <v-row dense>
                                    <v-col cols="3"
                                           v-for="page in pages" :key="page.index"
                                    >
                                        <v-card flat tile v-if="page.hasPreview" class="page_card">
                                            <div style="height: 275px; position: relative">
                                                <v-img :src="page.previewUrl" height="275" contain/>
                                                <div style="position: absolute; height: 100%; width: 100%; top:0; z-index: 1;"
                                                     class="d-flex justify-center align-center"
                                                >
                                                    <v-progress-circular
                                                            v-if="page.loading"
                                                            indeterminate
                                                            style="z-index: 2"
                                                    ></v-progress-circular>
                                                    <div v-else
                                                         class="page_controls flex-column justify-space-between"
                                                         style="width: 100%; height: 100%; z-index: 2">
                                                        <div/>
                                                        <div class="d-flex justify-space-between px-2">
                                                            <v-btn class="" color="red" fab small
                                                                   @click="deletePage(page)">
                                                                <v-icon>delete</v-icon>
                                                            </v-btn>
                                                            <v-btn class="" color="success" fab small
                                                                  @click="goToEdit(page.id)">
                                                                <v-icon>edit</v-icon>
                                                            </v-btn>
                                                        </div>
                                                        <div class="text-center">
                                                            <v-btn class="" color="primary" small
                                                                   @click="setCover(page)">
                                                                Set cover
                                                            </v-btn>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>

                                            <div class="text-center">{{ page.filename }}</div>
                                        </v-card>
                                    </v-col>
                                </v-row>
                            </v-container>
                        </v-card-text>
                        <v-card-actions>
                            <v-file-input show-size counter multiple label="Pages upload"
                                          v-model="uploaderModel"></v-file-input>
                            <!--                            <v-btn color="blue" tile outlined><v-icon left>add</v-icon> Add page</v-btn>-->
                        </v-card-actions>
                    </v-card>
                </v-col>
            </v-row>
        </v-container>
    </fragment>
</template>

<script>
    import {mapGetters, mapMutations} from "vuex";
    import PagesSorterDialog from "../components/pages_sorter/PagesSorterDialog";
    import Sorter from "../components/pages_sorter/PagesSorter";

    export default {
        name: "SeriesCreateEdit",
        components: {PagesSorterDialog, Sorter},
        data() {
            /*
            coverUrl
            title
            author
            description
            status
            owner
            tags
             */
            return {
                valid: true,
                loading: false,
                id: null,
                title: "New series",
                description: "",


                /* *
                * previewUrl
                * loading
                * progress
                * filename
                * id
                * */
                pages: [],
                uploaderModel: [],
                nextIndex: 1,

                cover: null,

                sortDialog: false,
                sortableItems: [],
            }
        },
        computed: {
            isNew() {
                return !this.id;
            },
            canUpload() {
                return !!this.id
            },
            titleRules() {
                return [
                    v => (v || '').length > 0 || `Title is required`
                ]
            },
            hasCover() {
                return !!this.cover;
            },
            ...mapGetters(["firstPageOfSeries", "numberOfPages", "hasPages", "currentSeriesId", "pagesList"])
        },
        watch: {
            title: 'validateField',
            uploaderModel: 'handleUpload',
            pagesList(newList, oldList) {
                const sortedPages = [];
                let notFound = false;
                newList.forEach((id) => {
                    const p = this.pages.find((p) => p.id === id);
                    if (!p) {
                        notFound = true;
                    }
                    sortedPages.push(p);
                });
                if (!notFound) {
                    this.pages = sortedPages;
                }
            }
        },
        methods: {
            ...mapMutations(['setReturnTo', 'sortPagesFilename', 'sortPagesManualMode']),
            async sortPages() {
              this.sortPagesFilename();
              await this.$store.dispatch('savePagesOrder');
            },
            sortPagesManual() {
                this.sortableItems = this.pages.map((p) => {
                   return {
                       id: p.id,
                       src: p.previewUrl
                   }
                });
                this.sortDialog = true;
            },
            async commitManualSort() {
                this.sortPagesManualMode(this.sortableItems.map((p) => p.id));
                await this.$store.dispatch('savePagesOrder');
                this.sortDialog = false;
            },
            goToEdit(pageId) {
                this.setReturnTo(this.$route);
                this.$router.push({
                    name: "ImageEditWithSeries", params: {
                        seriesId: this.currentSeriesId,
                        pageId: pageId,
                    }
                })
            },
            save() {
                if (this.isNew) {
                    this.createNew().then((id) => {
                        this.id = id;
                        this.$router.replace({name: 'SeriesEdit', params: {id: id}})
                    })
                } else {
                    this.$store.dispatch("saveSeries", {
                        id: this.id,
                        info: {
                            title: this.title,
                            description: this.description,
                            coverId: this.cover ? this.cover.id : undefined,
                        },
                    })
                }
            },
            startReading() {
                const firstPage = this.firstPageOfSeries;
                this.setReturnTo(this.$route);
                this.$router.push({
                    name: "ImagePreviewWithSeries", params: {
                        seriesId: this.currentSeriesId,
                        pageId: firstPage,
                    }
                })
            },
            validateField() {
                this.$refs.form.validate()
            },
            editPage(page) {
                this.$router.push({name: 'ImageEdit', params: {id: page.id}})
            },
            deletePage(page) {
                if (this.cover === page) {
                    this.cover = null;
                }
                const index = this.pages.indexOf(page);
                this.pages.splice(index, 1)
                this.$store.dispatch('deletePage', { id: page.id })
            },
            setCover(page) {
                this.cover = page;
            },
            handleUpload(files) {
                console.log(files);
                if (!files || files.length === 0) return;

                for (const file of files) {
                    const page = {
                        filename: file.name,
                        progress: 0,
                        loading: true,
                        previewUrl: null,
                        hasPreview: false,
                        id: "",
                        index: this.nextIndex,
                    };
                    this.nextIndex++;
                    this.pages.push(page);
                    const reader = new FileReader();
                    reader.addEventListener("load", () => {
                        page.previewUrl = reader.result;
                        page.hasPreview = true;
                        this.$store.dispatch("uploadPage", {
                            file: file,
                            seriesId: this.id,
                        }).then(({id, previewUrl}) => {
                            page.id = id;
                            page.hasPreview = true;
                            page.loading = false;
                        });

                    });
                    reader.readAsDataURL(file);
                }
            },
            async createNew() {
                this.loading = true;
                const {id} = await this.$store.dispatch("saveSeries", {
                    info: {
                        title: this.title,
                        description: this.description
                    }
                });
                this.loading = false;
                return id
            }
        },
        created() {
            this.id = this.$route.params.id;
            if (this.id) {
                this.$store.dispatch("loadSeries", {id: this.id, setCurrent: true})
                    .then(({info, pages = []}) => {
                        this.title = info.title;
                        this.description = info.description;
                        this.pages = pages.map((p) => {
                            return {
                                id: p.id,
                                previewUrl: p.previewUrl,
                                filename: p.originalFileName,
                                hasPreview: true,
                            }
                        });
                        if (info.coverId) {
                            this.cover = this.pages.find(p => p.id === info.coverId)
                        }
                    })
            }
        },

    }
</script>

<style lang="scss">
    .page_card .page_controls {
        display: none !important;
    }

    .page_card:hover {
        & .page_controls {
            display: flex !important;
        }
    }
</style>