<template>
    <div class="toolbar py-1 d-flex align-center justify-space-between">
        <div class="w-33p d-flex justify-start align-center overflow-hidden">
            <v-btn icon @click="scaleUp" :disabled="!canScaleUp">
                <v-icon>zoom_in</v-icon>
            </v-btn>
            <v-btn icon @click="scaleDown" :disabled="!canScaleDown">
                <v-icon>zoom_out</v-icon>
            </v-btn>
            {{ displayScale }}
            <span class="px-sm-5"/>
            <v-btn icon @click="displayAll = !displayAll">
                <v-icon v-if="displayAll">visibility_off</v-icon>
                <v-icon v-else>visibility</v-icon>
            </v-btn>
        </div>
        <div class="w-33p d-flex justify-center">
            <div class="d-flex w-100" style="max-width: 150px" v-if="showPager">
                <v-btn icon @click="page--" :disabled="page === 1">
                    <v-icon>chevron_left</v-icon>
                </v-btn>
                <v-select dense solo :items="pages" v-model="page" hide-details

                />
                <v-btn icon @click="page++" :disabled="page === numberOfPages">
                    <v-icon>chevron_right</v-icon>
                </v-btn>
            </div>
        </div>
        <div class="w-33p d-flex justify-end">
            <v-btn icon @click="$emit('close')" v-if="showCloser">
                <v-icon>close</v-icon>
            </v-btn>
        </div>
    </div>
</template>

<script>
    import {floatBetween} from "./utils";

    export default {
        name: "PreviewerToolbar",
        props: {
            numberOfPages: {
                type: Number,
            },
            currentPage: {
                type: Number,
            },
            showPager: {
                type: Boolean,
                default: () => true
            },
            showCloser: {
                type: Boolean,
                default: () => true
            },
        },
        data() {
            return {
                scale: 1,
                displayAll: false,
                pages: [],
                page: 1,
            }
        },
        methods: {
            scaleUp() {
                this.scale += 0.1;
                this.$emit('scale', this.scale)
            },
            scaleDown() {
                this.scale -= 0.1;
                this.$emit('scale', this.scale)
            },
            setPages(newNumber) {
                const pages = [];
                for (let i = 1; i <= newNumber; i++) {
                    pages.push(i)
                }
                this.pages = pages;
                if (pages.indexOf(this.page) === -1) {
                    this.page = 1;
                }
            }
        },
        computed: {
            displayScale() {
                return (this.scale * 100).toFixed(0) + "%";
            },
            canScaleDown() {
                return this.scale - 0.1 >= 0.2
            },
            canScaleUp() {
                return !(this.scale + 0.1 > 4)
            },
        },
        watch: {
            displayAll(val) {
                this.$emit('visibility', val)
            },
            numberOfPages(newNumber) {
                this.setPages(newNumber)
            },
            currentPage(page) {
                this.page = page;
            },
            page(nextPage) {
                this.$emit('page', nextPage)
            }
        },
        created() {
            this.setPages(this.numberOfPages);
        }
    }
</script>

<style scoped>
    .w-33p {
        width: 33.3%;
    }

    .w-100p {
        width: 100%;
    }
</style>