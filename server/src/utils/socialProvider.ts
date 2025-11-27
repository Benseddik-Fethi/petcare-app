import axios from 'axios';

interface SocialProfile {
    email: string;
    firstName: string;
    lastName: string;
    avatar?: string;
    id: string;
}

// 1. VÉRIFICATION GOOGLE (Via Access Token)
export const verifyGoogleToken = async (token: string): Promise<SocialProfile> => {
    try {
        const { data } = await axios.get('https://www.googleapis.com/oauth2/v3/userinfo', {
            headers: { Authorization: `Bearer ${token}` }
        });

        return {
            email: data.email,
            firstName: data.given_name,
            lastName: data.family_name,
            avatar: data.picture,
            id: data.sub
        };
    } catch (error) {
        console.error("Erreur vérification Google:", error);
        throw new Error("Google authentication failed");
    }
};

// 2. VÉRIFICATION FACEBOOK (Via Access Token)
export const verifyFacebookToken = async (token: string): Promise<SocialProfile> => {
    try {

        const { data } = await axios.get(
            `https://graph.facebook.com/me?fields=id,email,first_name,last_name,picture.type(large)&access_token=${token}`
        );

        const avatarUrl = data.picture?.data?.url;
        return {
            email: data.email,
            firstName: data.first_name,
            lastName: data.last_name,
            avatar: avatarUrl,
            id: data.id
        };
    } catch (error) {
        console.error("Erreur vérification Facebook:", error);
        throw new Error("Facebook authentication failed");
    }
};
