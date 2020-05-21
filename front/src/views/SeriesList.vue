<template>
    <fragment>
        <v-dialog width="400" v-model="signInShown">
            <v-snackbar v-model="loginError" :timeout="2000" color="red">{{ error }}</v-snackbar>
            <v-card>
                <v-card-text>
                    <v-container>
                        <form>
                            <v-row>
                                <v-col cols="12">
                                    <v-text-field label="Username" required v-model="username"
                                    autocomplete="username"/>
                                </v-col>
                                <v-col cols="12">
                                    <v-text-field label="Password" type="password" required v-model="password"
                                    autocomplete="current-password"/>
                                </v-col>
                            </v-row>
                        </form>
                    </v-container>
                </v-card-text>
                <v-divider/>
                <v-card-actions>
                    <v-btn color="green" @click="doSignIn">Sign in</v-btn>
                    <v-btn color="red" @click="signInShown = false">Cancel</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
        <v-app-bar dense>
            <v-toolbar-title>My Otakumole</v-toolbar-title>
            <v-spacer/>
            <v-btn @click="signInShown = true" v-if="isGuest">
                Sign in
            </v-btn>
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
    import {mapState, mapGetters, mapMutations, mapActions} from 'vuex';

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
                signInShown: false,
                username: "",
                password: "",
                loginError: false,
            }
        },
        watch: {
            isGuest(isGuest) {
                if (this.signInShown && !isGuest) {
                    this.signInShown = false
                }
            },
            lastErrorTime(has) {
                this.loginError = true;
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
            ...mapGetters(['firstPageOfSeries', "lastErrorTime", 'isAuthenticated', 'isGuest', "error", "hasError"]),
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
            doSignIn() {
                this.signIn({username: this.username, password: this.password})
            },
            ...mapMutations(['setReturnTo']),
            ...mapActions(['signIn'])
        },
        created() {
            this.$store.dispatch('loadListing');
        }
    }
</script>

<style scoped>

</style>