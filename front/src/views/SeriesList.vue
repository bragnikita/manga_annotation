<template>
    <fragment>
        <v-app-bar dense>
            <v-toolbar-title>My Otakumole</v-toolbar-title>
            <v-spacer/>
            <v-btn icon @click="createNew" color="green">
                <v-icon>add</v-icon>
            </v-btn>
        </v-app-bar>
        <v-container>
            <v-row dense v-if="loading">
                <v-col sm="12">
                    <v-skeleton-loader width="100%" type="card-heading, image, paragraph" class="mb-3"/>
                    <v-skeleton-loader width="100%" type="card-heading, image, paragraph" class="mb-3"/>
                    <v-skeleton-loader width="100%" type="card-heading, image, paragraph" class="mb-3"/>
                </v-col>
            </v-row>
            <v-row dense v-else>
                <v-col sm="12"
                       v-for="item of itemsDisplay" :key="item.id">
                    <v-card class="" style="min-width: 350px; max-width: 800px; margin: auto">
                        <v-card-title>
                            <a :href="'series/' + item.id" @click.prevent="gotoRead(item)">{{item.title}}</a>
                        </v-card-title>
                        <v-card-text>
                            <div class="d-flex justify-start" v-if="item.hasCover"
                                 style="overflow: hidden"
                            >
                                <img :src="item.coverUrl" alt=""
                                     style="max-height: 400px; min-height: 300px"
                                />
                            </div>
                        </v-card-text>
                        <v-card-text style="white-space: pre-line">{{item.description}}</v-card-text>
                        <v-card-actions>
                            <v-btn color="orange" text @click="gotoRead(item)">Read</v-btn>
                            <v-btn color="orange" text @click="gotoEdit(item)">Edit</v-btn>
                        </v-card-actions>
                    </v-card>
                </v-col>
            </v-row>
        </v-container>
    </fragment>
</template>

<script>
    import {mapState, mapGetters, mapMutations} from 'vuex';

    export default {
        name: "SeriesList",
        components: {},
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
                items: [],
            }
        },
        computed: {
            itemsDisplay() {
                return this.listing.map((item) => {
                    return {
                        ...item,
                        hasCover: !!item.coverUrl,
                    }
                });
            },
            ...mapState(['loading', 'listing']),
            ...mapGetters(['firstPageOfSeries']),
        },
        methods: {
            gotoRead(series) {
                this.$store.dispatch("loadSeries", {id: series.id, setCurrent: true})
                    .then(() => {
                        const firstPage = this.firstPageOfSeries;
                        if (firstPage) {
                            this.setReturnTo(this.$route);
                            this.$router.push({
                                name: "ImagePreviewWithSeries", params: {pageId: firstPage, seriesId: series.id}
                            })
                        }
                    });
            },
            gotoEdit(series) {
                this.$router.push({name: "SeriesEdit", params: {id: series.id}})
            },
            createNew() {
                this.$router.push({name: "SeriesCreate"})
            },
            ...mapMutations(['setReturnTo'])
        },
        created() {
            this.$store.dispatch('loadListing');
            // this.items = [
            //     {
            //         id: 1,
            //         coverUrl: '/samples/sample1.png',
            //         title: 'Magia record ch 1',
            //         author: 'Fujino Fuji',
            //         status: 'done',
            //         description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris est ligula, porttitor a ornare quis, ullamcorper nec urna. Fusce a tempus urna, vel faucibus mauris. Sed id ullamcorper tellus. Duis suscipit ligula magna. Donec varius velit ultricies, lobortis magna placerat, mattis mi. Donec in lectus accumsan, sagittis neque id, gravida ligula. Suspendisse non scelerisque magna. Nulla consectetur risus in ullamcorper porta. Aenean tempus placerat faucibus. Duis eros eros, porttitor non dictum vitae, pharetra eget ligula. Etiam euismod molestie convallis. Nunc congue maximus purus non euismod.',
            //     },
            //     {
            //         id: 2,
            //         coverUrl: 'https://picsum.photos/300/200',
            //         title: 'Princess club',
            //         status: 'in_progress',
            //         description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean eros odio, feugiat sit amet rhoncus at, luctus nec turpis. Phasellus eleifend orci ac tristique dignissim. Suspendisse porttitor, est ac porttitor posuere, est dolor scelerisque purus, quis vehicula elit elit in metus. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi ac viverra eros. Donec et velit sit amet magna accumsan hendrerit vel eget odio. Fusce pulvinar ligula et sapien luctus pharetra. Nunc varius malesuada malesuada.'
            //     }
            // ]
        }
    }
</script>

<style scoped>

</style>