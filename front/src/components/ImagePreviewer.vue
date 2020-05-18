<template>
    <div>
        <div class="image_container" @resize="reimport">
            <img id="image_target" :src="imageUrl" @load="onLoaded"
                 :style="styleImg"
            />
            <div v-for="bubble of bubblesMarkers" :key="bubble.id"
                 :style="bubble.areaPositionStyle" :class="['bubble_marker']"
                 @mouseover="show(bubble)"
            >
            </div>
            <div v-for="bubble of bubblesToDisplay" :class="['commited_bubble']"
                 @mouseleave="hide(bubble)" :style="[bubble.areaPositionStyle,
                 bubble.styles]"
            ><div class="bubble-text-wrapper" :style="bubble.textStyle">
                {{ bubble.text }}
            </div>
            </div>
        </div>
    </div>
</template>

<script>
    import Vue from "vue";

    export default {
        name: "ImagePreviewer.vue",
        props: {
            imageUrl: {
                type: String,
            },
            translation: {
                type: Object,
            },
            displayAll: {
                type: Boolean,
            },
            maxWidth: {
                type: Number,
                default: 2000,
            },
            scale: {
                type: Number,
                default: 1,
            }
        },
        data() {

            return {
                bubbles: [],
                displayingNow: [],
                styleImg: {
                    width: this.getImgScale(this.scale)
                }
            }
        },
        methods: {
            onLoaded() {
                this.$img = jQuery('#image_target');
                this.reimport();
            },
            reimport() {
                console.log("reimport");
                this.bubbles = this.importFromJson(this.translation)
            },
            importFromJson(json) {
                if (!json) return [];
                const { annotations } = json;
                const iWidth = this.$img.width();
                const iHeight = this.$img.height();
                return annotations.map((annotation) => {
                    const a = annotation.area_relative;
                    const x = Math.ceil(a.x * iWidth);
                    const width = Math.floor(a.width * iWidth);
                    const y = Math.ceil(a.y * iHeight);
                    const height = Math.floor(a.height * iHeight);

                    const ratio = iWidth / annotation.orig_image.width;
                    const textStyle = {
                      transform: `scale(${ratio})`
                    };

                    return {
                        id: annotation.id,
                        x, y, width, height,
                        z: 100 + annotation.id,
                        text: annotation.text,
                        styles: annotation.styles,
                        areaPositionStyle: {
                            top: y + "px",
                            left: x + "px",
                            height: height + "px",
                            width: width + "px",
                        },
                        textStyle,
                    }
                });
            },
            show(bubble) {
                this.displayingNow.push(bubble.id)
            },
            hide(bubble) {
                const index = this.displayingNow.findIndex((b) => b.id === bubble.id);
                this.displayingNow.splice(index ,1)
            },
            getImgScale(value) {
                let imgWidth = "100%";
                if (value) {
                    imgWidth = (value * 100) + '%';
                }
                return imgWidth;
            }
        },
        computed: {
            bubblesMarkers() {
                if (this.displayAll) return [];
                return this.bubbles.filter((b) => !this.displayingNow.includes(b.id));
            },
            bubblesToDisplay() {
                if (this.displayAll) return this.bubbles;
                return this.bubbles.filter((b) => this.displayingNow.includes(b.id));
            },

        },
        watch: {
            scale(newValue) {
                this.styleImg.width = this.getImgScale(newValue);
                Vue.nextTick().then(() => {
                    this.reimport();
                });
            },
        },
        created() {
            window.addEventListener("resize", this.reimport);
        },
        destroyed() {
            window.removeEventListener("resize", this.reimport);
        }

    }
</script>

<style scoped lang="scss">
    .image_container {
        max-width: 1000px;
        position: relative;
        img {
            width: 100%;
            max-width: min-content;
        }
    }
    .bubble_marker {
        position: absolute;
        border: darkred double;
        border-radius: 4px;
        opacity: 0.3;
    }
</style>