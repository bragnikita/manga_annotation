<template>
    <div>
        <div class="mb-1">
            <v-select dense outlined v-model="fontFamily" :items="fonts" solo hide-details />
        </div>
        <div class="wrapper d-flex justify-start">
            <v-btn color="green lighten-3" fab x-small @click="fontUp">
                <v-icon>format_size</v-icon>
            </v-btn>
            <v-btn color="red lighten-3" fab x-small @click="fontDown">
                <v-icon>text_fields</v-icon>
            </v-btn>
        </div>
    </div>
</template>

<script>
    export default {
        name: "EditorToolbar",
        props: {
          value: {
              type: Object,
              default: () => ({
                  fontFamily: 'AnimeAce',
                  fontSize: 10,
              })
          }
        },
        data() {
            return {
                fonts: ['AnimeAce', 'ComicScriptRussBold', 'monospace', 'sans-serif'],
                fontFamily: 'AnimeAce',
                fontSize: 10,
                ...this.value,

                result: {},
            }
        },
        methods: {
            fontUp() {
                this.fontSize += 1;
            },
            fontDown() {
                this.fontSize -= 1;
            },
        },
        computed: {
            styles() {
                return {
                    fontFamily: this.fontFamily,
                    fontSize: this.fontSize,
                }
            }
        },
        watch: {
            value(newValue) {
                this.fontSize = newValue.fontSize;
                this.fontFamily = newValue.fontFamily || 'sans-serif';
            },
            styles(newStyle) {
                this.$emit('input', newStyle)
            }
        }
    }
</script>

<style scoped lang="scss">
    .wrapper {
        & > * {
            margin-right: 5px;
        }
    }
</style>